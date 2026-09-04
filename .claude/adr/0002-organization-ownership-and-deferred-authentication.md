# ADR 0002 — Organization ownership on Vacancy, with authentication deferred

**Status**: Accepted (2026-08-02, by the human developer — accepted as proposed, without
modification)

**Date**: 2026-08-02

**Context feature**: `specs/001-create-vacancy`

## Context

`spec.md` assumes: *"Authentication and organization-scoped authorization are provided by existing
platform infrastructure and are assumed already available; this feature only specifies that creation
requires them."*

**That assumption is false.** Verified against the repository:

- `jobzy-api/pom.xml` declares no `spring-boot-starter-security` and no
  `spring-boot-starter-oauth2-resource-server`.
- There is no `SecurityConfig` or any Spring Security configuration anywhere.
- The strings `organization`, `security` and `tenant` do not occur anywhere under `jobzy-api/src`.
- `VacancyApi.yml` declares a global `bearerAuth` JWT requirement and states that "multi-tenancy
  (organization scoping) is derived from this token, never from the request or response body" — a
  requirement nothing currently enforces.

So FR-006 ("MUST restrict vacancy creation to authenticated users belonging to the owning
organization, rejecting unauthenticated or unauthorized attempts") has no foundation to build on, and
the two edge cases in the spec about unauthenticated and wrong-organization requests cannot be
implemented as written.

The expensive part is not the 401/403 enforcement. It is the **data model**. The spec defines Vacancy
as "a job opening owned by an organization". If vacancies are created now without an owning
organization, then introducing multi-tenancy later requires backfilling `organizationId` on every
existing row (with no correct value available to backfill from) and adding a tenant predicate to
every query that ever reads a vacancy. The Constitution's Mission section warns specifically against
this class of retrofit.

## Decision

Split FR-006 into its data-model half and its enforcement half, and deliver only the first now.

1. **`Vacancy` carries `organizationId` from day one** — on the aggregate, on the JPA entity, and as a
   non-nullable column in the schema. The data model is correct immediately; nothing needs
   backfilling later.

   **`OrganizationId` is a real value object, not a bare `UUID`** (clarified 2026-08-02 after the
   question was raised during T003 — the original wording was ambiguous about whether the type name
   was literal or shorthand). It is literal:

   ```java
   package app.jobzy.api.common.domain;

   public record OrganizationId(UUID value) {
     public OrganizationId {
       Objects.requireNonNull(value, "organizationId must not be null");
     }
   }
   ```

   Three reasons, in order of weight:

   - **It is the tenant discriminator.** Of every field on this aggregate, this is the one where a
     mix-up is most expensive: passing the wrong organization id is cross-tenant data leakage, the
     highest-severity bug class in a multi-tenant ATS. A bare `UUID` parameter silently accepts a
     user id, a `GenerationId`, or a future candidate id — all of which are UUIDs too.
   - **ADR 0003 §4 already implies it.** It justifies `VacancyId` as a wrapper precisely to stop
     "mixing vacancy ids with generation ids and organization ids". Typing one side of that sentence
     and not the other applies the rationale by half.
   - **Consistency with ADR 0001 part B.** The human chose the stricter, explicitly-typed variant
     there; defaulting to a bare `UUID` here would pull the codebase in the opposite direction one
     decision later.

   **It lives in `app.jobzy.api.common.domain`, not in `vacancy/domain/`.** An organization is not a
   vacancy-owned concept — the organization owns the vacancy, not the reverse — and every subsequent
   aggregate (candidate, application, pipeline event) needs the same type. Putting it under
   `vacancy/` would invert the ownership and guarantee a move later, at the point the codebase has
   the most consumers. `common/` already exists in this feature's layout for `GlobalExceptionHandler`
   and `ClockConfig`, so this introduces no new top-level concept. The zero-framework rule applies to
   `common/domain` exactly as it does to `vacancy/domain`.

   *Alternative considered*: put it in `vacancy/domain/` and move it when the second aggregate needs
   it. Cheaper today by one package, and genuinely reversible — but the move is predictable rather
   than hypothetical, since Epic 1 is explicitly a multi-aggregate applicant pipeline.

   **`OrganizationId` wraps a plain `UUID` with no generation strategy of its own.** It is *not*
   UUIDv7 — that requirement applies only to `VacancyId`, and only because the contract's cursor
   pagination depends on time-ordering (ADR 0003 §4). Do not pull the UUIDv7 generator in for this
   type; the fixed adapter simply parses a configured constant.

2. **The organization is never read from the request body.** It is obtained through a narrow
   core-owned outbound port:

   ```java
   public interface OrganizationContextPort {
     OrganizationId currentOrganizationId();
   }
   ```

   This matches the contract's explicit statement that org scoping comes from the token, not the
   body, and it means `CreateCoreVacancyCommand` gains no organization field (it must not — a client
   could then forge it).

3. **The only implementation today is a temporary fixed adapter**,
   `adapter/out/organization/FixedOrganizationContextAdapter`, returning a single configured
   development organization id. It carries a `TODO` naming this ADR and must be replaced by a
   JWT-claim-reading adapter when authentication lands. When that happens, **this one class is the
   only thing that changes** — the aggregate, the schema, the use case and the controller are already
   correct.

4. **401/403 enforcement is explicitly deferred** to a separate authentication feature with its own
   spec. Until then the endpoint is effectively open.

### Options considered

**Option A — build full JWT resource-server authentication now.**
Correct and complete; FR-006 fully satisfied. But it means introducing Spring Security, an issuer and
JWKS configuration, org-claim extraction, an organization model, and a security-filter test strategy
— all inside a feature whose stated scope is "create a vacancy". This is a separate feature by any
reasonable measure and would multiply the size of this one. Rejected as scope creep (Principle V,
Principle VI).

**Option B — ship creation with no organization concept at all.**
Cheapest today. Rejected: it defers a data migration over every vacancy row plus a change to every
read query, and there is no correct value to backfill with. Strictly more expensive later than a
column now, and it is the exact retrofit the Mission section forbids.

**Option C (chosen) — correct data model now, enforcement deferred behind a port.**
Gets the irreversible part (schema and aggregate shape) right immediately at near-zero cost, and
isolates the reversible part (where the org id comes from) behind a single-method port with one
throwaway implementation.

## Consequences

### The one that needs explicit human acceptance

**FR-006 is only partially delivered by this feature.** The endpoint will accept unauthenticated
requests and will attribute every vacancy to the same fixed development organization. The spec's two
authorization edge cases are not implemented. The definition of done for `001-create-vacancy` changes
accordingly, and `tasks.md` says so.

If that is unacceptable, authentication must be specced and built first, and this feature is blocked
behind it. **That is the human's call, not the architect's.**

### Other consequences

- The application is **not deployable to a public environment** in this state. It is fine behind a
  private dev environment. This must not reach production before the auth feature lands.
- A `TODO` referencing this ADR sits in `FixedOrganizationContextAdapter` so the debt is discoverable
  from the code rather than only from this document.
- Because the org id is sourced from a port rather than the request, adding real auth later cannot
  accidentally be bypassed by a client sending an `organizationId` field — there is no such field.
- `OrganizationId` is process data, not personal data (Principle III). It identifies a company, not a
  person, and is unaffected by candidate anonymization.
- The value-object clarification fixes three signatures: the `Vacancy` factory takes
  `OrganizationId` (T003), `OrganizationContextPort.currentOrganizationId()` returns it (T006), and
  `VacancyEntityMapper` unwraps it to a plain `UUID` for the `organization_id` column (T005) — the
  value object must not reach the JPA entity. Nothing on the DTO or web side changes: `VacancyDto`
  carries no `organizationId` at all (ADR 0001 part B), so T007, T009, T010 and T011 are unaffected
  apart from the type flowing through `CreateVacancyService`.
- No recurring cost impact (Principle VI): no identity provider is provisioned by this decision.
