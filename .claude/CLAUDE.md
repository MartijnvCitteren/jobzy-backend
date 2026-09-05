# CLAUDE.md

Instructions for Claude (Code) when working in the jobzy-backend repository. This file describes the *code* context — for the
product/market "why" and the roadmap, see project knowledge (product/market vision, H2 2026 epics).

## What Jobzy is
Jobzy is a recruitment hub — ATS, channel-advice engine, and later a marketplace — replacing fragmented point tools. The
advice engine depends entirely on ATS pipeline data (channel source, rejection reason, stage transitions), so those must be
captured accurately from day one. Build order is dependency-driven: ATS core → multiposting via an aggregator → channel
dashboard with rule-based advice; marketplace and payment mediation are explicitly phase 2. Don't build marketplace, payment
mediation, or a full flow-builder now — out of scope until the ATS core and advice engine prove out.

**This drives how you work here:**

- Momentum > perfection. Pick the smallest change that advances the current epic; don't let scope creep beyond what was
  asked.
- Deliberate over-engineering is fine when it serves an explicit learning goal (DDD/Hexagonal, OCP 21 track). Without a
  learning goal: take the pragmatic route.
- Keep recurring costs (Azure, external APIs like the aggregator in epic 4) low — flag it if a change affects that.

## Tech stack

- **Java 25**, **Spring Boot 4.0.x** (Spring Framework 7). Boot 4 has Java 17 as its minimum baseline but first-class support
  for 25 (JSpecify null-safety, modular jars) — use those features where it makes sense, but check library compatibility (not
  every Spring dependency is equally far along with Java 25/virtual threads).
- **Maven** as build tool, multi-module reactor.
- **Azure** (Container Apps for dev/prod, custom domain jobzy.app) as target environment.


## Repository structure (multi-module)

Root modules (`pom.xml` `<modules>`): **`jobzy-contracts`** and **`jobzy-api`**. Root package for both:
`app.jobzy`.

- **`jobzy-contracts`** — the API contract as source of truth. Contains the OpenAPI/YAML spec(s) (e.g.
  `src/main/java/app/jobzy/contracts/VacancyApi.yml`); shared models and API interfaces are generated from these via
  `openapi-generator-maven-plugin`.
    - **Never** hand-edit generated classes. Changes always go through the YAML.
    - Contract changes are breaking-change-sensitive as soon as there's a consumer outside this monorepo (multiposting
      aggregator, future integrations) — treat the YAML with the same care as a published API, even though there's no
      external customer yet.
    - Codegen is configured in `jobzy-api/pom.xml` (`openapi-generator-maven-plugin`, bound to `generate-sources`), with
      generated model/API packages under `app.jobzy.api.<aggregate>.adapter.in.*` — i.e. codegen output already lands at
      the adapter edge, not under `domain`. Keep it that way: a generated model landing under a `domain` package is a bug,
      not a style nit (see ADR 0001).
- **`jobzy-api`** — the application core, built following **DDD + Hexagonal (Ports & Adapters)**. Actual package layout
  under `app.jobzy.api.<aggregate>` (e.g. `app.jobzy.api.vacancy`, root shared code under `app.jobzy.api.shared`):
    - `domain/<aggregate>/` — entities, value objects (`domain/<aggregate>/valueobject/`), domain services, domain
      events. **Zero** framework dependencies, no Spring annotations.
    - `application/service/` — use-case orchestration and transactions.
    - `application/port/in/` (with `application/port/in/command/` for inbound command DTOs) and `application/port/out/`
      — ports owned by the core.
    - `adapter/in/rest/<aggregate>/` — inbound REST adapter, with `mapper/request/` and `mapper/response/` subpackages
      for explicit contract-DTO ↔ domain mapping.
    - `adapter/out/persistence/<aggregate>/` — outbound JPA/Postgres adapter, with its own `mapper/` subpackage.
    - `shared/exception/` — cross-cutting exception types (e.g. `GlobalExceptionHandler`) not specific to one aggregate.
    - Adapters know the domain, never the reverse. Generated `jobzy-contracts` models belong at the `adapter/in/rest`
      edge, not in the domain model — map explicitly between contract DTOs and domain models, never leak contract types
      into `domain` or `application`.

## Architecture principles for changes

- Apply tactical DDD where the domain is interesting (matching, scoring, screening, retention/anonymization — see epic 2, an
  explicit learning hook for domain modeling). For CRUD-ish edges: no dogma, just be pragmatic.
- New external integrations (aggregator, LLM providers) always go behind a port with an adapter — never inject the SDK/client
  directly into a use case.
- For an architecture decision with real trade-offs: capture it as a short ADR instead of deciding it in code alone.
- GDPR/personal data: process data (events, channel source, rejection reason) and personal data (name, CV, email) are
  deliberately separated in the data model so anonymization can wipe the latter while leaving the former intact. New
  candidate-related fields: decide explicitly which category they belong to before adding them.

## Build & test

```
mvn clean install                         # full build incl. contract codegen
mvn -pl jobzy-contracts -am generate-sources   # regenerate contract only
mvn test                                  # unit tests, all modules
mvn -pl jobzy-api test                    # tests for the api module only
```

Use Maven/the linter for style and compile errors — not Claude as a linter. Run existing tests/checks yourself via bash
rather than relying on your own judgment of correctness. `mvn clean install` is not cheap — run it once per verification
pass, not repeatedly "to be sure"; only re-run it if you have a concrete new reason to suspect flakiness (a fresh change
to test isolation/config), not because an area was flaky once before.

## Speckit specs

Feature specs live at repo root: `specs/<NNN-feature-slug>/` (`spec.md`, `plan.md`, `tasks.md`, `checklists/`) — **not**
under `.specify/specs/`. `.specify/` holds the Speckit tool machinery itself (templates, scripts, and
`.specify/memory/constitution.md`, the project constitution — the canonical, more detailed source for the principles
summarized in this file; when the two disagree, the constitution wins and this file should be updated to match).
Existing ADRs live in `.claude/adr/`.

## Language policy (strict)

**Everything in this codebase is in English.** No exceptions:

- **Code**: identifiers, method/class/variable names, log messages, exception messages.
- **Javadoc and comments**: English only.
- **Commit messages and PR descriptions**: English only.
- **ADRs and in-repo docs**: English only.

Dutch is fine in conversation with human Developer but never leaks into anything that ends up in the repository.

## Working style in this repo

- Be critical and direct in code review and proposals — no cheerleading. If an approach is weak or lets scope creep, say so.
- When torn between the "clean/academic variant" and the "pragmatic variant": name both with trade-offs, make the choice
  explicit.
- 