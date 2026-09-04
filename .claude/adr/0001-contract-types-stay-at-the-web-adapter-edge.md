# ADR 0001 — Contract types stay at the web adapter edge; use-case ports are framework-free

**Status**: Accepted (2026-08-02, by the human developer)

**Date**: 2026-08-02

**Context feature**: `specs/001-create-vacancy`

## Context

Constitution Principle II requires that generated contract models stay at the adapter/REST edge and
are explicitly mapped to domain models, and that ports are owned by the core and free of framework
dependencies. The scaffolding currently in `main` breaks both rules.

**Part A — where generated models live.** `jobzy-api/pom.xml` configures codegen with:

```xml
<modelPackage>app.jobzy.api.vacancy.domain.rest</modelPackage>
<apiPackage>app.jobzy.api.vacancy.adapter.in.web</apiPackage>
```

Every generated contract model therefore lands under a package named `domain`. The contract types are
not just adjacent to the domain — by package name they *are* the domain. Anyone reviewing by package
structure, or any future ArchUnit rule expressed in terms of `..domain..`, reads this as compliant
when it is precisely inverted.

**Part B — what the use case returns.** The existing inbound port is:

```java
public interface CreateVacancyUseCase {
  ResponseEntity<VacancyResponse> createCoreVacancy(VacancyCoreRequestDto createCoreVacancyCommand);
}
```

`ResponseEntity` is a Spring Web type and `VacancyResponse` is a generated contract type, both inside
a core-owned port. This makes the application layer untestable without Spring and puts the HTTP
status-code decision in the wrong layer. It must change. The open question is what it changes *to*.

There is an asymmetry to be aware of: the codebase already has an inbound application DTO layer
(`CreateCoreVacancyCommand`, `LocationDto`, `VacancyCategoryDto`, `WorkplaceTypeDto`) that insulates the
core from `VacancyCoreRequest`. So the input side has already been built in the stricter style.

## Decision

**Part A.** Change `modelPackage` to `app.jobzy.api.vacancy.adapter.in.web.model`. Generated contract
models then live inside the web adapter, where Principle II says they belong. `apiPackage` is already
correct. Update imports in the four referencing files: `VacancyController`, `VacancyCoreRequestMapper`,
`CreateVacancyUseCase`, `VacancyCoreRequestMapperTest`.

Do this before any domain or application code is written. It costs four import blocks today and grows
monotonically with every feature added on top of the wrong package.

**Part B — DECIDED: a symmetric outbound application DTO.** The port becomes:

```java
public interface CreateVacancyUseCase {
  VacancyDto createCoreVacancy(VacancyCoreRequestDto createCoreVacancyCommand);
}
```

The domain aggregate never leaves the application layer in either direction. New types, both in
`app.jobzy.api.vacancy.application.port.in.dto` (the return type of an inbound port belongs to that
port's DTO package):

- `VacancyDto` — record of `UUID id`, `String jobTitle`, `VacancyCategoryDto category`,
  `LocationDto location`, `WorkplaceTypeDto workplaceType`, `BigDecimal hoursPerWeek`,
  `VacancyStatusDto status`, `OffsetDateTime createdAt`. Reuses the existing `LocationDto`,
  `VacancyCategoryDto` and `WorkplaceTypeDto`.
- `VacancyStatusDto` — enum `DRAFT, PUBLISHED, FILLED, CLOSED, ARCHIVED`.

Two constraints on `VacancyDto` that must not be missed:

- It carries a plain `UUID`, **not** the domain's `VacancyId` value object. Passing `VacancyId` would
  defeat the entire point of the DTO.
- It does **not** carry `organizationId`. `VacancyResponse` has no such field and the organization
  must never reach the wire (see ADR 0002).

Mapping `Vacancy` → `VacancyDto` happens in the application layer via a MapStruct `VacancyDtoMapper`
in `application/mapper/`, consistent with the mapper pattern already used in this codebase.
MapStruct's `componentModel = "spring"` puts a `@Component` on the generated impl, which is
acceptable: the application layer already carries `@Service` and `@Transactional`. Principle II's
zero-framework rule applies to the **domain** package, not to the application layer.

The controller owns the HTTP concerns: it maps `VacancyDto` to `VacancyResponse` via
`VacancyResponseMapper` (in the web adapter), sets `201 Created`, and builds the `Location` header.

### Options considered for Part B

**Option 1 — return the domain aggregate.** (Originally recommended by the architect; **rejected** by
the human.)

- Framework-free and contract-free, so compliant with Principle II.
- No third representation of the same eight fields; one mapper hop out instead of two.
- Cost: the domain aggregate crosses the port boundary outward. If `Vacancy` later grows internals
  that should not be exposed (domain events, internal state machines), the web adapter's mapper is
  reading a type richer than it needs.

**Option 2 — a symmetric outbound application DTO. (CHOSEN)**

- Textbook-correct: the domain never leaves the application layer in either direction.
- Symmetric with the inbound DTO layer that already exists, so the codebase reads consistently — a
  codebase that is strict inbound and loose outbound is harder to reason about than one that is
  uniformly either.
- Aligns with the explicit DDD/Hexagonal learning goal that Principle II names, which is the stated
  exception in Principle V to the pragmatism default.

### Rationale for choosing Option 2 over the architect's recommendation

The architect recommended Option 1 on Principle V grounds (an eight-field pure-copy DTO with no
behaviour is ceremony). The human overrode this, and the override is well-founded: Principle V permits
deliberate over-engineering *when it serves an explicit learning goal*, and Principle II names
DDD/Hexagonal as exactly such a goal for this codebase. Consistency with the existing inbound DTO
layer was the deciding factor.

**Accepted cost, recorded explicitly**: two extra types (`VacancyDto`, `VacancyStatusDto`), one extra
mapper (`VacancyDtoMapper`) and its test, and one extra mapping hop per request — for a feature with
eight fields and no behaviour in the DTO. This is a real cost, knowingly taken, and it sets the
pattern every subsequent use case in this codebase is expected to follow.

## Consequences

- Generated models move package; a full `mvn clean install` is required after T001 because stale
  generated sources under the old package will otherwise linger in `target/`.
- `CreateVacancyService` becomes testable with plain Mockito and no Spring context, as the
  unit-testing skill requires.
- `VacancyResponseMapper` in the web adapter maps **`VacancyDto` → `VacancyResponse`**, not
  `Vacancy` → `VacancyResponse`. No domain type appears anywhere in the web adapter.
- Three mapping hops per request in total:
  `VacancyCoreRequest` → `CreateCoreVacancyCommand` → `Vacancy` → `VacancyDto` → `VacancyResponse`.
  The first and last hops are in the web adapter; the middle two are in the application layer.
- HTTP status codes and the `Location` header are decided in exactly one place, the controller.
- A future ArchUnit rule can be stated simply: nothing under `..domain..` may import `..adapter..`,
  `..application..` or `org.springframework..`; nothing under `..application..` may import
  `..adapter..`. That rule is currently unenforceable because of Part A.
- Every future use case in this codebase should follow the same in-DTO/out-DTO shape. Deviating
  per-feature would reintroduce exactly the inconsistency this decision was made to avoid.
