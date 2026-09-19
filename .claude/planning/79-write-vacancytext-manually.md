# Plan — GitHub issue #79: Write vacancy description manually

**Feature**: `POST /vacancy/{id}/description` (contract already finalized in `VacancyApi.yml`,
operationId `manualVacancyDescription`). Not a Speckit feature — this plan + issue #79 is the spec.

Architecture decisions (persistence shape, source tracking, validation location) are recorded in
[`.claude/adr/0004-manual-vacancy-description-persistence-and-validation.md`](../adr/0004-manual-vacancy-description-persistence-and-validation.md)
— **two decisions were overridden by the human after the architect's initial pass**:

1. **Decision 1** (persistence shape): separate `VacancyDescriptionJpaEntity`/table (the issue's
   original proposal), not the architect's originally-recommended embedded value object.
2. **Decision 3** (validation location): content-safety validation (raw HTML/script tags, control
   characters) lives in the **REST adapter layer**, not the domain `VacancyDescription` value object —
   fail-fast at the edge, before any domain/application object is constructed. The domain VO is now a
   plain data holder with no invariant-checking logic for this concern.

Read the ADR first; the summary and task breakdown below reflect both human decisions, not the
architect's initial recommendations.

## Summary of what's being built

- New domain value object `VacancyDescription` (5 optional text fields + `source`) — a **plain data
  holder**, no validation logic. Framework-free.
- New adapter-level validator `VacancyDescriptionContentValidator` (in `adapter/in/rest/vacancy`),
  invoked from the controller before the request is mapped to a command — rejects (does not strip) raw
  HTML/script-like tags and disallowed control characters, naming the offending field.
- New `VacancyDescriptionJpaEntity` / `VacancyDescriptionJpaRepository`: own table, own primary key,
  unique `vacancyId` column as FK (plain `UUID`, no `@OneToOne`), five text columns, `source`,
  `createdAt`/`updatedAt` (persistence-only, not in the domain VO).
- `Vacancy` gains a `description` field, and `Vacancy.Builder` gains an `id(UUID)` override (see
  Task 1 — pre-existing gap, not new scope, but it blocks this feature).
- `VacancyRepository` (existing port, **not** a new one — see ADR Decision 1) gains `findById`;
  `VacancyRepositoryAdapter` internally loads/writes both `VacancyJpaEntity` and
  `VacancyDescriptionJpaEntity` inside its existing `findById`/`save` methods. The application layer
  never sees that persistence spans two tables.
- New use case `ManualVacancyDescriptionUseCase` / `SetManualVacancyDescriptionService`: load the
  vacancy by id (404 if missing), replace its description in full, persist, return the updated
  aggregate. By the time a request reaches this layer, content is already known-valid.
- New exceptions `VacancyNotFoundException` (domain) and `InvalidVacancyDescriptionRequestException`
  (adapter-level, thrown by the new validator, not by the domain VO), and three new
  `GlobalExceptionHandler` handlers (404, 400, generic 500 fallback) — none of these exist today.
- REST: wire `VacancyController.manualVacancyDescription`, invoking the validator before mapping to a
  command, plus one request mapper and one response mapper.

## Risks / open items for the human (do not guess on these)

1. **`source = GENERATED` reachability** — see ADR 0004 Decision 2. Flag to #72's implementer, not
   blocking this feature.
2. **ADR 0003 vs. reality: Flyway never actually landed.** Confirmed deferred as a follow-up, not in
   scope for #79. The new `vacancy_description` table is created by `ddl-auto: update`, same as every
   other table today.
3. **ADR 0003 §6 vs. reality: no enum column has `@Enumerated(EnumType.STRING)`.** Confirmed deferred
   as a follow-up. `VacancyDescriptionSource` is persisted the same (undecorated) way
   `VacancyCategory`/`VacancyStatus` already are — not fixed here, to avoid a third inconsistent case.
4. **ADR 0001 Part B vs. reality: no `VacancyDto` exists.** `CreateVacancyUseCase` already returns the
   raw domain `Vacancy` aggregate directly (not the `VacancyDto` ADR 0001 mandated). This plan follows
   the *actual shipped* pattern (return `Vacancy` from the new use case too) for consistency, not the
   ADR's mandate. Still an open flag for the human to formally supersede ADR 0001 Part B or retrofit
   `VacancyDto` everywhere — not decided here, not blocking.
5. **Upsert-by-`vacancyId` correctness.** `VacancyDescriptionJpaEntity` has its own primary key,
   distinct from `vacancyId`. The adapter must look up the existing row by `vacancyId` before writing,
   and reuse its primary key on update — inserting blindly would accumulate duplicate rows per vacancy
   and silently break "manual write overwrites any existing description." Task 5/6 below build and test
   this explicitly; flagging here because it's the one place a naive implementation would look correct
   in a single-write test and only fail on the second write.
6. **Content-safety validation no longer self-protects the domain type.** Since Decision 3 moved
   validation to the REST adapter, `VacancyDescription` will silently accept invalid content if
   constructed by a path that skips the validator. Flagged explicitly in ADR 0004 Decision 3 for #72's
   implementer (`PATCH /vacancy/{id}` must call the same validator independently) — not blocking this
   feature, but a real structural risk of the fail-fast-at-the-edge approach worth carrying forward.

## Task breakdown

Each task is independently testable (TDD: write the failing test first, then the code). No
per-task review checkpoint — the developer self-verifies with tests as they go; review happens once,
at the end, via the trailing `[review-gate]` task.

---

### Task 1 — Fix `Vacancy.Builder` to allow reconstructing an existing id
**Implements:** prerequisite for #79 (not a numbered Speckit task; this repo isn't using Speckit for
this feature).

`Vacancy.Builder` currently does `private final UUID id = UuidV7Generator.getUUID();` with no way to
override it. This blocks reconstructing a `Vacancy` from persistence (needed by Task 4) — every
`builder()` call mints a brand-new id, so `findById` can never return an aggregate with its real,
original id.

- File: `jobzy-api/src/main/java/app/jobzy/api/domain/vacancy/Vacancy.java`
- Change `private final UUID id = UuidV7Generator.getUUID();` to `private UUID id =
  UuidV7Generator.getUUID();` and add `public Builder id(UUID id) { this.id = id; return this; }`.
- Default behavior (new vacancy, no explicit id) is unchanged — `CreateVacancyService` keeps working
  as-is.
- Test: `jobzy-api/src/test/java/app/jobzy/api/domain/vacancy/VacancyTest.java` — add a case asserting
  `Vacancy.builder().id(someUuid)....build().getId()` returns exactly `someUuid`, and a case asserting
  the no-`id(...)`-call path still auto-generates a non-null id (likely already covered, verify).

---

### Task 2 — `VacancyDescription` and `VacancyDescriptionSource` domain value objects (plain data holder — REVISED)
**Implements:** #79 "source = MANUAL" bookkeeping and the general need for the aggregate to carry a
description. **Revised by the human's Decision 3 override**: this VO no longer validates content —
that moved to the REST adapter (Task 9a). Much smaller task than originally planned.

- New file: `jobzy-api/src/main/java/app/jobzy/api/domain/vacancy/valueobject/VacancyDescriptionSource.java`
  — plain enum `MANUAL, GENERATED`. No framework imports.
- New file: `jobzy-api/src/main/java/app/jobzy/api/domain/vacancy/valueobject/VacancyDescription.java`
  — record `(String summary, String jobDescription, String tasks, String whatWeOffer, String aboutUs,
  VacancyDescriptionSource source)`. **No compact-constructor validation** — a plain data holder. Do
  not add content-safety or max-length checks here (see ADR 0004 Decision 3).
- No `InvalidVacancyDescriptionException` in this package anymore — the equivalent exception now lives
  in the REST adapter (Task 9a) since that's where the check runs.
- Test: no dedicated validation test suite needed here (there is no invariant to test). If the
  developer wants a trivial construction/equality test for the record, that's optional and low-value —
  don't invest TDD effort validating a plain data carrier with no behavior.

---

### Task 3 — `Vacancy` domain aggregate gains a `description` field
**Implements:** #79 (the aggregate needs to carry its description regardless of storage).
**Unchanged by either human override.**

- File: `jobzy-api/src/main/java/app/jobzy/api/domain/vacancy/Vacancy.java`
- Add `private VacancyDescription description;` with getter/setter, and `Builder.description(...)`
  (default `null`, matching the existing "core-only at creation" flow — `CreateVacancyService` is
  unaffected).
- Test: extend `VacancyTest.java` — setter/getter round-trip, default-null on builder without
  `.description(...)`.

---

### Task 4 — `VacancyNotFoundException` and `VacancyRepository.findById` (two-table load)
**Implements:** #79 acceptance criterion "unknown vacancy id → 404", and loading the full aggregate
(vacancy + its description, if any) through the existing, single `VacancyRepository` port — see ADR
0004 Decision 1 for why this is *not* a second port. **Unchanged by the Decision 3 validation-location
override.**

- New file: `jobzy-api/src/main/java/app/jobzy/api/domain/vacancy/VacancyNotFoundException.java` —
  extends `BaseException`, constructed from the missing `UUID id`, message naming the id.
- File: `jobzy-api/src/main/java/app/jobzy/api/application/port/out/VacancyRepository.java` — add
  `Optional<Vacancy> findById(UUID id);`. No change to the port's shape otherwise — it still only
  speaks in terms of `Vacancy`, nothing description-specific leaks into the port signature.
- File: `jobzy-api/src/main/java/app/jobzy/api/adapter/out/persistence/vacancy/VacancyRepositoryAdapter.java`
  — implement `findById`: fetch `VacancyJpaEntity` via `jpaRepository.findById(id)`; if present, also
  fetch the optional `VacancyDescriptionJpaEntity` via the new `VacancyDescriptionJpaRepository`
  (Task 5) using `findByVacancyId(id)`; combine both into one domain `Vacancy` via the mapper (Task 5's
  `toDomain(VacancyJpaEntity, VacancyDescriptionJpaEntity)`, `VacancyDescriptionJpaEntity` may be
  `null` for a vacancy that never had a description set — must map to a `null` domain `description`,
  not throw). Since `VacancyDescription` no longer validates on construction (Task 2), reconstructing
  it from persisted data is a pure, safe mapping regardless of content.
- Test: extend `jobzy-api/src/test/java/app/jobzy/api/adapter/out/persistence/vacancy/mapper/VacancyJpaMapperTest.java`
  for the two-argument `toDomain` overload — one case with a description entity present (asserts every
  field including `source` round-trips), one case with `null` description entity (asserts the domain
  `Vacancy.getDescription()` is `null`, not an exception).

---

### Task 5 — `VacancyDescriptionJpaEntity`, its repository, and mapper
**Implements:** #79 persistence — this is the task that changed most from the architect's original
(embedded) plan, per the human's Decision 1 override.

- New file: `jobzy-api/src/main/java/app/jobzy/api/adapter/out/persistence/vacancy/VacancyDescriptionJpaEntity.java`
  — own `@Id UUID id` (assigned via `UuidV7Generator`, same identity policy as `Vacancy` itself per ADR
  0003 §4 — not `@GeneratedValue`), `@Column(unique = true) UUID vacancyId`, `summary`,
  `jobDescription`, `tasks`, `whatWeOffer`, `aboutUs` (plain `String` columns, no `@Embeddable`
  involved), `source` (`VacancyDescriptionSource`, undecorated — see risk #3, don't add
  `@Enumerated` here alone), `createdAt`/`updatedAt` (`LocalDateTime`, persistence-only — not modeled
  in the domain VO, set by the mapper: `createdAt` only on first insert, `updatedAt` on every write).
- New file: `jobzy-api/src/main/java/app/jobzy/api/adapter/out/persistence/vacancy/VacancyDescriptionJpaRepository.java`
  — `extends JpaRepository<VacancyDescriptionJpaEntity, UUID>` with `Optional<VacancyDescriptionJpaEntity>
  findByVacancyId(UUID vacancyId);` (derived query).
- New file/methods: `jobzy-api/src/main/java/app/jobzy/api/adapter/out/persistence/vacancy/mapper/VacancyDescriptionJpaMapper.java`
  — `VacancyDescription toDomain(VacancyDescriptionJpaEntity entity)` (handle `null` in the caller, not
  here — keep the mapper a pure 1:1 mapping) and a method to build/update a
  `VacancyDescriptionJpaEntity` from `(UUID vacancyId, VacancyDescription description, @Nullable
  VacancyDescriptionJpaEntity existing)` — **must reuse `existing.getId()` and `existing.getCreatedAt()`
  when `existing` is non-null**, only generating a fresh id (and setting `createdAt`) when `existing` is
  `null`. This is the upsert logic flagged in risk #5 — write the test in Task 6 first to pin this down.
- Extend `VacancyJpaMapper` (or keep it focused and add the combining logic directly in the adapter —
  developer's call) with the two-argument `toDomain(VacancyJpaEntity, VacancyDescriptionJpaEntity)`
  used by Task 4.
- No migration file — `ddl-auto: update` creates the new table, consistent with how every existing
  table got there (see risk #2; do not add Flyway as part of this task).

---

### Task 6 — Persistence round-trip test, including the upsert case
**Implements:** verification of ADR 0004 Decision 1's two-table shape, specifically the upsert-by-
`vacancyId` correctness flagged as risk #5 — this is the part most likely to look correct on a single
write and silently break on the second.

- New file: `jobzy-api/src/test/java/app/jobzy/api/adapter/out/persistence/vacancy/VacancyRepositoryAdapterTest.java`
  (`@DataJpaTest`-style against H2, following the pattern the unit-testing skill and ADR 0003 §3
  establish). Cases:
  - Save a `Vacancy` with a fully-populated `VacancyDescription` (all 5 fields + `source = MANUAL`),
    reload via `findById`, assert every field round-trips exactly, and assert exactly one row exists in
    `VacancyDescriptionJpaRepository` for that `vacancyId`.
  - Save the same vacancy again with a *different* description, reload, assert the new content
    replaced the old, and assert there is still exactly **one** row for that `vacancyId` in
    `VacancyDescriptionJpaRepository` (not two) — this is the case that catches a naive
    always-insert implementation.
  - Save a vacancy with no description at all, reload via `findById`, assert
    `vacancy.getDescription()` is `null` and no row exists in `VacancyDescriptionJpaRepository`.

---

### Task 7 — `GlobalExceptionHandler`: 404, 400 for invalid content, and generic 500 fallback
**Implements:** #79 acceptance criteria "unknown vacancy id → 404", "raw HTML/control characters → 400,
nothing persisted", and "database failure during save → 5xx Problem Details, no partial write."
**Adjusted for the Decision 3 override**: the 400 handler now targets an adapter-level exception type,
not a domain one. None of these three handlers exist today (the handler currently only covers
`MethodArgumentNotValidException`), so this closes a real, pre-existing gap that also silently affects
`CreateVacancyService`'s DB-failure path, not just this feature.

- File: `jobzy-api/src/main/java/app/jobzy/api/adapter/in/rest/GlobalExceptionHandler.java`
- Add `@ExceptionHandler(VacancyNotFoundException.class)` → 404 `ProblemDetails` (`application/problem+json`).
- Add `@ExceptionHandler(InvalidVacancyDescriptionRequestException.class)` → 400 `ProblemDetails` with a
  single-entry `errors` list naming the offending field (reuse the same `errors` shape as the existing
  validation handler). This exception now comes from Task 9a's adapter-level validator, imported from
  `adapter.in.rest.vacancy` — a same-layer import, no boundary concern (`GlobalExceptionHandler` itself
  is already in `adapter.in.rest`).
- Add a generic `@ExceptionHandler(Exception.class)` fallback → 500 `ProblemDetails` ("An unexpected
  error occurred while processing the request", no internals leaked in `detail`). This is what makes
  a DB failure during `save()` (e.g. `DataAccessException` propagating out of
  `VacancyRepositoryAdapter`, now touching two tables) surface as RFC 9457 Problem Details instead of
  Spring's default error response. Atomicity across the two tables is already guaranteed by Task 8's
  `@Transactional` service method (see ADR 0004 Decision 1) — this handler only needs to translate the
  exception, not manage the rollback.
- Tests: extend `GlobalExceptionHandlerTest.java` with one case per new handler (unit-level, same style
  as the existing test — construct the exception, call the handler method directly, assert status/body).

---

### Task 8 — Application use case: `ManualVacancyDescriptionUseCase` / `SetManualVacancyDescriptionService`
**Implements:** #79 happy flow, "manual write overwrites any existing LLM-generated description",
"database failure during save → no partial write". **Adjusted for the Decision 3 override**: by the
time a command reaches this service, content has already passed the adapter-level validator (Task 9a),
so the service has no content-validation responsibility and no invalid-content test case — it can
assume the command it receives is safe.

- New file: `jobzy-api/src/main/java/app/jobzy/api/application/port/in/command/SetManualVacancyDescriptionCommand.java`
  — record `(UUID vacancyId, String summary, String jobDescription, String tasks, String whatWeOffer,
  String aboutUs)`.
- New file: `jobzy-api/src/main/java/app/jobzy/api/application/port/in/ManualVacancyDescriptionUseCase.java`
  — `Vacancy setManualDescription(SetManualVacancyDescriptionCommand command);` (returns the domain
  `Vacancy`, matching the pattern actually used by `CreateVacancyUseCase` today — see risk #4, not
  ADR 0001's `VacancyDto`).
- New file: `jobzy-api/src/main/java/app/jobzy/api/application/service/SetManualVacancyDescriptionService.java`
  — `@Service @Transactional`: `vacancyRepository.findById(...).orElseThrow(() -> new
  VacancyNotFoundException(...))`, build a `VacancyDescription` with `source = MANUAL` from the
  command, `vacancy.setDescription(...)`, `vacancyRepository.save(vacancy)`, return `vacancy`. Full
  replace, not a merge — matches the contract's "replaces ... in full" wording, so no need to preserve
  any prior field-by-field. `@Transactional` here is what makes the adapter's two-table write atomic
  (ADR 0004 Decision 1) — do not add redundant transaction management inside the adapter.
- Test (TDD, write first): `jobzy-api/src/test/java/app/jobzy/api/application/service/SetManualVacancyDescriptionServiceTest.java`
  — Mockito, no Spring context, following `CreateVacancyServiceTest.java`'s style. Cases: happy path
  (repository returns a vacancy, service sets description with `source = MANUAL`, saves, returns
  updated vacancy); unknown id (`findById` returns `Optional.empty()`) → `VacancyNotFoundException`
  propagates, `save` never called; repository `save` throws → exception propagates unmodified (no
  swallowing, matching ADR 0003 §5's existing pattern for `CreateVacancyService`).

---

### Task 9 — REST adapter mappers
**Implements:** #79, request/response mapping at the adapter edge. **Unchanged by either human
override** — the contract-facing shape never changed, and mapping is a separate concern from
validation (Task 9a).

- New file: `jobzy-api/src/main/java/app/jobzy/api/adapter/in/rest/vacancy/mapper/request/VacancyDescriptionRequestMapper.java`
  — MapStruct `@Mapping(target = "vacancyId", source = "id") SetManualVacancyDescriptionCommand
  toCommand(UUID id, VacancyDescriptionRequest request);` (field names otherwise match by name). Purely
  structural — assumes the request has already passed Task 9a's validator.
- New file: `jobzy-api/src/main/java/app/jobzy/api/adapter/in/rest/vacancy/mapper/response/VacancyDescriptionResponseMapper.java`
  — MapStruct `VacancyDescriptionResponse toResponse(VacancyDescription description);`, with
  `unmappedSourcePolicy = ReportingPolicy.IGNORE` (the domain `source` field has no contract
  counterpart — deliberate, see ADR 0004 Decision 2) and `unmappedTargetPolicy = ReportingPolicy.WARN`.
- Tests: `VacancyDescriptionRequestMapperTest.java` and `VacancyDescriptionResponseMapperTest.java`
  under the matching `adapter/in/rest/vacancy/mapper/{request,response}` test packages, following
  `VacancyCoreRequestMapperTest.java`'s style.

---

### Task 9a — REST adapter content validator (NEW, per the human's Decision 3 override)
**Implements:** #79 acceptance criterion "content containing raw HTML/script tags or control
characters → 400, nothing persisted" — this is the entire task that moved out of the domain layer.

- New file: `jobzy-api/src/main/java/app/jobzy/api/adapter/in/rest/vacancy/validation/VacancyDescriptionContentValidator.java`
  — a plain Spring `@Component` (or static utility, developer's call) with a method like `void
  validate(VacancyDescriptionRequest request)` (or one call per field — developer's call on shape) that
  checks each non-null field for a raw HTML/script-like tag construct or a disallowed control character
  (allow `\n`, `\r`, `\t`; reject other C0/C1 control characters), throwing (not stripping) on the first
  violation found. Does **not** re-check max length (already covered by generated `@Size` at the web
  edge, unchanged from the original plan).
- New file: `jobzy-api/src/main/java/app/jobzy/api/adapter/in/rest/vacancy/InvalidVacancyDescriptionRequestException.java`
  — extends `app.jobzy.api.shared.exception.BaseException`, carries the field name and message (used by
  Task 7's `GlobalExceptionHandler` to populate `ProblemDetails.errors`). Lives in the adapter package
  now, not `domain/vacancy/valueobject` — this is deliberate, matching where the check now runs.
- Invocation point: called explicitly from `VacancyController.manualVacancyDescription` (Task 10),
  immediately after receiving the request and before calling the request mapper (Task 9) or the use
  case (Task 8) — this is what makes the "fail before any domain/application object is constructed"
  property in ADR 0004 Decision 3 actually true. Do not bury the call inside the mapper via a MapStruct
  hook — keep it an explicit, visible step in the controller so the fail-fast ordering is obvious to a
  reader.
- Tests (TDD, write first): `jobzy-api/src/test/java/app/jobzy/api/adapter/in/rest/vacancy/validation/VacancyDescriptionContentValidatorTest.java`
  — the same case list originally planned for the domain VO test, now here instead: valid plain text
  (including multi-line via `\n`) accepted; `<script>...</script>`, `<b>`, `</div>`, `<!--` rejected; a
  bare control character rejected; `\n`/`\t`/`\r` accepted; a false-positive check that ordinary text
  containing a lone `<` not forming a tag (e.g. `"revenue < target"`) is **accepted**; all fields null
  (fully empty description) accepted; each of the 5 fields validated independently, with the exception
  naming the correct field.

---

### Task 10 — Wire `VacancyController.manualVacancyDescription`
**Implements:** #79 end-to-end. **Adjusted for the Decision 3 override**: now also calls the new
validator (Task 9a) as an explicit first step.

- File: `jobzy-api/src/main/java/app/jobzy/api/adapter/in/rest/vacancy/VacancyController.java`
- Replace the `return null;` stub: **validate** the incoming `VacancyDescriptionRequest` (Task 9a's
  validator — first step, before anything else) → map request → command (Task 9's request mapper) →
  call `ManualVacancyDescriptionUseCase.setManualDescription` (Task 8) → map
  `vacancy.getDescription()` → `VacancyDescriptionResponse` (Task 9's response mapper) →
  `ResponseEntity.status(HttpStatus.CREATED).body(response)`, matching the `201` the contract declares.
  Inject the validator, mapper, and use case via constructor (`@RequiredArgsConstructor` already in
  place).
- Update the class Javadoc comment listing which operations are wired (currently says "Only
  `#createVacancy` is wired up so far").

---

### Task 11 — Integration test: `POST /vacancy/{id}/description`
**Implements:** #79 acceptance criteria end-to-end, black-box through the real H2-backed stack.

- File: `jobzy-api/src/test/java/app/jobzy/api/integration/vacancy/VacancyControllerIntegrationTest.java`
  (extend existing, following its `RestAssured` + H2 pattern) or a new
  `VacancyManualDescriptionIntegrationTest.java` in the same package if the existing file is getting
  large — developer's call.
- Cases:
  - Valid description for an existing vacancy → `201`, response body matches, `source` persisted as
    `MANUAL` (assert via the new `VacancyDescriptionJpaRepository.findByVacancyId(...)`, not via the
    response — the contract never exposes `source`).
  - Manual write overwrites a previously-set description (seed one, POST a different one, assert the
    old content is gone **and** there is still exactly one row for that `vacancyId` — this is the
    integration-level companion to Task 6's adapter-level upsert test).
  - Raw HTML/control-character content → `400`, `application/problem+json`, and
    `VacancyDescriptionJpaRepository` shows the description **unchanged** from before the request (or
    no row at all, if it's the first write) — nothing persisted. This exercises Task 9a's validator
    end-to-end, confirming the request never even reaches the use case.
  - Content exceeding a field's max length → `400` (already covered by generated `@Size` +
    `MethodArgumentNotValidException`, but add one case here for end-to-end confidence on this specific
    endpoint).
  - Unknown vacancy id → `404`, `application/problem+json`.
  - (Optional, developer's judgment on cost/value) DB-failure → `500`: only if it can be done without
    new test infrastructure (e.g. a Mockito-mocked repository bean profile) — do not introduce
    Testcontainers or similar to force a real DB failure, per ADR 0003 §3's no-Docker constraint. The
    unit-level coverage in Task 8 already proves the exception propagates; Task 7 already proves the
    handler maps it to 500. An integration test here is nice-to-have, not required for the AC to be
    considered met.

---

### Task 12 — `[review-gate] Final review: manual-vacancy-description`
**Depends on:** every task above (1, 2, 3, 4, 5, 6, 7, 8, 9, 9a, 10, 11).

Single trailing checkpoint. No other review/doc task exists in this plan — the
`jobzy-code-reviewer` and `jobzy-documentation-writer` each run exactly once here, against the full
accumulated diff for #79.

**Status: DONE. Reviewed-by: jobzy-code-reviewer** — verdict APPROVE (2026-09-05). Full-diff review
against all three axes (coding standards, architecture, functionality) plus one verification
`mvn clean install` run (78 tests, 0 failures). All ADR 0004 decisions confirmed in code; all
issue #79 acceptance criteria confirmed exercised by tests. Only non-blocking nitpicks (a couple of
fully-qualified inline references instead of imports) — no fix pass required before proceeding to
documentation.
