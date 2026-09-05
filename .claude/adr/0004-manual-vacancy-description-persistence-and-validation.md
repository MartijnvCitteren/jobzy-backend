# ADR 0004 — Manual vacancy description: persistence shape, source tracking, and validation location

**Status**: Accepted (2026-09-05, by the human developer — Decision 1 overrides the architect's
original recommendation, see below)

**Date**: 2026-09-05

**Context feature**: GitHub issue #79 ("Write vacancytext manually"), branch `feature/79-write-vacancytext-manually`

## Context

Issue #79 asks for a manual, non-LLM path to set a vacancy's `description`. The issue's own body
proposes a dedicated `VacancyDescription` JPA entity (1:1 with `Vacancy`, unique FK, `source`
MANUAL/GENERATED column), reasoning: keeps `Vacancy`'s own table lean for list/summary queries, and
makes room for a `source` distinction and later history/versioning.

That proposal was written **before** `jobzy-contracts/.../VacancyApi.yml` was finalized. The shipped
contract already decided the API shape differently:

- `VacancyDescriptionResponse` is a flat object with five independent, all-optional plain-text fields
  (`summary`, `jobDescription`, `tasks`, `whatWeOffer`, `aboutUs`), each with its own `maxLength`. No
  `source` field anywhere in the contract.
- `VacancyResponse.description` embeds `VacancyDescriptionResponse` directly — not a link/reference to
  a sub-resource. `VacancyListResponse.items` is an array of full `VacancyResponse`, i.e. **the list
  endpoint already returns the full description inline for every item** — so the issue's "keep list
  queries lean" rationale for a separate table is weaker than it looks once the list endpoint is
  actually implemented: the description columns need to be fetched (via join or a second query) for
  every row regardless of which table they live in.
- `POST /vacancy/{id}/description` "replaces the vacancy's current description in full" — this is a
  same-aggregate full-replace, not an independent resource with its own lifecycle, from the API's point
  of view.

Given that, this ADR decides three coupled questions for the `jobzy-api` implementation.

## Decision 1 — Persistence shape: separate `VacancyDescription` entity and table (DECIDED BY THE HUMAN, OVERRIDING THE ARCHITECT)

**Decision**: a dedicated `VacancyDescriptionJpaEntity` / table, 1:1 with `Vacancy`, matching the
issue's original proposal: its own primary key, a unique `vacancyId` column as the FK, the five text
fields, `source`, and `createdAt`/`updatedAt` (persistence-only bookkeeping, not modeled in the domain
value object). No JPA relationship annotation (`@OneToOne`) between the two entities — the FK is a
plain `UUID` column, looked up via `findByVacancyId`, to avoid JPA relationship-mapping surprises
(lazy-loading proxies, cascade semantics) for what is, from the domain's point of view, a single
aggregate load.

**Repository/port shape**: folded into the *existing* `VacancyRepository` port and
`VacancyRepositoryAdapter`, rather than adding a second port. `VacancyRepository.findById` /
`.save` still operate purely on the domain `Vacancy` aggregate (which owns a `description` field); the
adapter internally loads/writes both tables. This follows the DDD convention of one repository per
aggregate root — `VacancyDescription` is a value object that lives inside the `Vacancy` aggregate
boundary, not an independently addressable entity, matching how the contract itself treats it. The
alternative (a standalone `VacancyDescriptionRepository` port + adapter) was considered and rejected:
it would let the application layer bypass the aggregate root and manipulate part of `Vacancy`'s state
directly, which is exactly what "one repository per aggregate" exists to prevent, for a persistence
detail (two tables under one aggregate) that the application layer has no reason to know about.

**Atomicity**: no new transactional wiring needed. `SetManualVacancyDescriptionService` is
`@Transactional` (matching `CreateVacancyService`'s existing pattern per ADR 0003 §5); both JPA writes
issued from inside `VacancyRepositoryAdapter.save` happen inside that same transaction and are rolled
back together on failure. This satisfies the "no partial write" acceptance criterion without any
adapter-level transaction management.

**Upsert detail that must not be missed**: `VacancyDescriptionJpaEntity` has its own primary key,
distinct from `vacancyId`. Writing a new description for a vacancy that already has one must reuse the
existing row's primary key (look up by `vacancyId` first, then update) — inserting a fresh row every
time would silently accumulate duplicate rows per vacancy and break "manual write overwrites any
existing description," since a naive `findById`-by-`vacancyId`-does-not-exist read would only ever see
the oldest row unless the query is written correctly. See the plan's Task 6 for the round-trip test
covering this specifically.

- *Pragmatic alternative (the architect's original recommendation, rejected by the human)*: embed
  `VacancyDescription` as an `@Embedded` value object directly on `VacancyJpaEntity`, following the
  existing `Location`/`HoursPerWeek` pattern on that same entity. Cheaper today (no new entity,
  repository, or upsert-by-FK logic), and the contract's flat, always-inline description shape doesn't
  strictly require a separate table. Rejected because the human's call favors the issue's original,
  more textbook-DDD-aligned schema, which keeps `Vacancy`'s own table free of large text columns and
  gives a clean seam for future independent versioning/audit history of description edits — a
  deliberate choice to accept more ceremony now for that seam, consistent with CLAUDE.md's allowance
  for deliberate over-engineering when it serves the DDD/Hexagonal learning goal.

**Accepted cost, recorded explicitly**: one new JPA entity, one new JPA repository, one new mapper (or
mapper methods), and the upsert-by-`vacancyId` logic inside `VacancyRepositoryAdapter`, for a feature
whose API contract never treats the description as anything other than an inline part of `Vacancy`.

## Decision 2 — Source tracking (MANUAL vs. GENERATED)

**Decision** (unchanged by the human's Decision 1 override — only where it's stored changes): add
`VacancyDescriptionSource` (`MANUAL`, `GENERATED`) as a field on the `VacancyDescription` value object,
persisted as a column on the new `VacancyDescriptionJpaEntity` (not embedded — see Decision 1). Not
exposed anywhere on the contract (the contract has no `source` field) — this is internal state only,
set to `MANUAL` by this feature's use case.

**Open question the human should confirm, not decided here**: under the *current* contract, is
`GENERATED` ever actually reachable? `POST /vacancy/{id}/generate-description` never persists
automatically per its own description ("never persisted automatically... only then submits the
(possibly edited) result via `PATCH /vacancy/{id}`") — so the only two paths that ever persist a
description (`POST .../description` here, and `PATCH /vacancy/{id}` when #72 lands) both represent a
human having reviewed and submitted final text. It is plausible `source` never legitimately becomes
`GENERATED` under this contract as written, making the column speculative. It is kept because the
issue's acceptance criteria explicitly requires "source = MANUAL" to be a persisted, checkable fact,
and the cost of the column is one field — but flagging this so #72's implementer decides deliberately
whether `PATCH` ever sets `GENERATED`, rather than the field quietly going unused.

## Decision 3 — Validation location for "no raw HTML/script tags or control characters, reject don't strip" (REVISED BY THE HUMAN)

**Decision (revised)**: enforce this in the **REST adapter layer** (`adapter/in/rest/vacancy`), not in
the domain `VacancyDescription` value object. A dedicated validator (e.g.
`VacancyDescriptionContentValidator`) runs against the raw `VacancyDescriptionRequest` fields before
the request is mapped to a command or any domain/application object is constructed, throwing an
adapter-level exception (`InvalidVacancyDescriptionRequestException`, naming the offending field) on
violation. The domain `VacancyDescription` value object becomes a **plain data holder** — a record with
no invariant-checking constructor logic for this concern. Max-length validation is unchanged: still
enforced at the web edge by the generated `@Size` bean-validation annotations on
`VacancyDescriptionRequest`, combined with `@Valid` on the generated `VacancyApi` interface.

**Reason for the reversal (the human's)**: fail-fast. Rejecting bad input at the edge, before it is
even packaged into a command or touches the use case, is preferable to discovering the same problem
deep inside domain object construction — the domain constructor is the wrong place to first learn that
a request is malformed, since by then the request has already been accepted past the controller
boundary and partially processed.

- *Domain-level variant (originally decided, now the rejected alternative)*: validate inside
  `VacancyDescription`'s compact constructor. Textbook-DDD-correct in the sense that the domain
  invariant holds unconditionally, regardless of entry point, and the rule is enforced exactly once. Its
  real weakness, which motivated the reversal: the domain object still has to be *constructed* (with
  bad data) before the constructor can reject it, which happens after the controller has already
  received and started processing the request — later than a dedicated adapter-level check, and mixed
  in with domain construction rather than being a clean, early request-shape check.
- Since the generated `VacancyDescriptionRequest` DTO cannot be hand-edited (CLAUDE.md — codegen output
  is regenerated from the YAML), the validator is a plain Java class invoked explicitly (from the
  controller, before calling the mapper/use case), **not** a custom Bean Validation constraint annotated
  onto the generated DTO. A custom annotation would have to live on a wrapper/subclass to avoid touching
  generated code, which is more indirection than an explicit validator call for the same effect.

**Accepted risk, flagged for #72 (revised from the original domain-level framing)**: because the
`VacancyDescription` value object no longer self-protects, **any future second write path must
remember to invoke the same adapter-level validator independently** — the domain object will silently
accept invalid content if a caller skips the check. Concretely: when #72's `PATCH /vacancy/{id}` path
starts accepting a (possibly AI-generated, user-edited) description, its own REST adapter code must call
`VacancyDescriptionContentValidator` too, since the safety net that used to live in the domain
constructor (and would have caught this automatically, at the cost of failing on reconstruction from
persistence — see the superseded framing above) is gone. This is a real, structural risk of the
fail-fast-at-the-edge approach: the invariant is now enforced per entry point, not per domain type. Flag
this explicitly for #72's implementer, the same way Decision 2's `source`-reachability question is
flagged — not decided here, must not be silently forgotten.

## Consequences

- New JPA entity `VacancyDescriptionJpaEntity`, new `VacancyDescriptionJpaRepository`, and new/extended
  mapper code — this now genuinely needs all three, unlike the architect's originally-recommended
  embedded approach.
- `VacancyRepository`/`VacancyRepositoryAdapter` (existing, one-per-aggregate) absorb the two-table
  load/save internally; no second repository port is introduced. The application layer remains
  completely unaware that `Vacancy`'s persistence spans two tables.
- `Vacancy` (domain aggregate) gains a `description` field and a `Vacancy.Builder.id(UUID)` override —
  see the accompanying plan for why the latter is required (pre-existing gap, not something this ADR
  introduces).
- `GlobalExceptionHandler` gains three new handlers (404 for "vacancy not found", 400 for
  `InvalidVacancyDescriptionRequestException` — now an adapter-level exception, not a domain one — and
  a generic 500 fallback) that did not exist before this feature — see the plan for scope
  justification.
- The domain `VacancyDescription` value object carries no validation logic and is a plain data holder;
  content-safety validation is an adapter-layer concern with no automatic protection for future write
  paths — see Decision 3's accepted risk.
- Two pre-existing gaps surfaced while researching this decision, unrelated to this ADR's actual
  decisions, are deferred as follow-ups (confirmed by the human, not in scope for #79), not fixed or
  silently ignored: (a) ADR 0003 mandates Flyway + `ddl-auto: validate`, but the repository still has no
  Flyway dependency and `ddl-auto: update` is still active — the new `vacancy_description` table is
  created by `ddl-auto: update` like every other table today, not by a migration; (b) ADR 0003 §6
  mandates `@Enumerated(EnumType.STRING)` for enum columns, but no enum column in `VacancyJpaEntity`
  (or now `VacancyDescriptionJpaEntity`) currently carries that annotation.
