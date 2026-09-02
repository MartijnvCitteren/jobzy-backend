# Vacancy package structure — DDD/Hexagonal review

## Verdict on `domain` package placement
Yes, create `app.jobzy.api.vacancy.domain` — but it does not exist yet, and what currently
occupies that namespace is not the domain.

## Critical issue: contract types are generated into `vacancy.domain.rest`
The OpenAPI codegen (see `jobzy-contracts/.../VacancyApi.yml`) is configured to emit generated
DTOs into `app.jobzy.api.vacancy.domain.rest` (confirmed in
`target/generated-sources/openapi/.../vacancy/domain/rest/*.java`).

This is a direct violation of the hexagonal boundary this repo's `CLAUDE.md` prescribes:
- `VacancyController` imports `VacancyCoreRequest`, `VacancyResponse`, etc. from
  `vacancy.domain.rest` — fine for an adapter, **except** the package name claims these are
  domain types. They are not; they are the REST contract and must be treated as adapter-edge
  DTOs only.
- `CreateVacancyUseCase` (`application/port/in`) returns `VacancyResponse` — a generated contract
  type leaking into the application layer's inbound port. A use-case port must depend only on
  the domain/application model, never on the contract. This also means the application layer
  now transitively depends on the web/OpenAPI module — the dependency arrow points the wrong way.

**Fix:** repoint the codegen output package to something unambiguous, e.g.
`app.jobzy.api.vacancy.adapter.in.web.contract` (or a dedicated `generated` package under the
adapter), so nothing can mistake it for the domain. Then fix `CreateVacancyUseCase` to return a
domain/application type (see below), mapped to `VacancyResponse` only inside the controller/adapter.

## Recommended package layout

```
vacancy/
  domain/                          <- NEW. No Spring, no contract imports.
    Vacancy.java                   (aggregate root)
    VacancyId.java, JobTitle.java  (value objects, as warranted)
    VacancyStatus.java             (domain enum — distinct from the generated VacancyStatus)
    VacancyCategory.java, WorkplaceType.java  (if they carry domain behavior/invariants)

  application/
    port/in/
      CreateVacancyUseCase.java    <- returns a domain type or an application-owned result, not VacancyResponse
      dto/VacancyCoreRequestDto.java
    port/out/
      VacancyRepository.java       <- doesn't exist yet; add when persistence lands
    service/
      CreateVacancyService.java    <- implementation of the use case (currently missing/interface-only)

  adapter/
    in/web/
      VacancyController.java
      VacancyApi.java (generated)
      mapper/
        VacancyCoreRequestMapper.java   (contract -> application DTO)
        VacancyResponseMapper.java      (domain -> contract)
    out/persistence/                    <- add when persistence is introduced
```

## Other findings

1. **`CreateVacancyUseCase` is `@Service` on an interface.** `@Service` belongs on the
   implementation, not the port. An inbound port is a plain interface with zero Spring
   annotations; only its adapter/implementation is a bean.
2. **Inconsistent mapper package casing**: `mapper/VacancyResponse/` (PascalCase) vs.
   `mapper/vacancyCoreRequest/` (camelCase). Java package convention is all-lowercase; pick one
   (`mapper.response`, `mapper.request` or similar) and normalize.
3. **`VacancyResponseMapper` and `CreateVacancyUseCase` currently don't compile** (missing return
   type / undefined symbol) — unrelated to the architecture question, but blocks verifying any
   restructuring by build.
4. **DTOs under `application/port/in/dto`**: fine as the application-layer request model, as
   long as they stay free of contract types (currently true) and free of JPA/persistence types
   (also currently true).

## Immediate next step for "create a Vacancy domain object"
Add `vacancy/domain/Vacancy.java` as a plain Java aggregate (no annotations), modeling only the
invariants that matter now (e.g. a vacancy needs a job title, category, location, workplace type,
hours/week before it can be published). Do not model persistence or REST concerns in it. Map to it
from `CreateVacancyCommand` inside the (currently missing) use-case implementation, not in the
controller.
