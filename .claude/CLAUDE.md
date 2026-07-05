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

> Fill in once known: exact Maven module names/artifactIds, database (Postgres?), CI pipeline. This file should grow as that
> becomes concrete — don't guess, just add.

## Repository structure (multi-module)

- **`contractz`** — the API contract as source of truth. Contains the OpenAPI/YAML spec(s); shared models and API interfaces
  are generated from these (codegen during the build, likely via `openapi-generator-maven-plugin` or similar).
    - **Never** hand-edit generated classes. Changes always go through the YAML.
    - Contract changes are breaking-change-sensitive as soon as there's a consumer outside this monorepo (multiposting
      aggregator, future integrations) — treat the YAML with the same care as a published API, even though there's no
      external customer yet.
    - Regeneration happens via this module's Maven build (`mvn generate-sources` or the relevant lifecycle phase — confirm in
      the module pom).
- **`api`** (or similarly named) — the application core, built following **DDD + Hexagonal (Ports & Adapters)**:
    - Domain layer: entities, value objects, domain services, domain events. No framework dependencies (no Spring annotations
      in the domain).
    - Application/use-case layer: orchestration, transactions, invokes the domain via ports.
    - Ports: interfaces defined by the core (e.g. `CandidateRepository`, `JobBoardPublisher`).
    - Adapters: infrastructure implementations of those ports (JPA/Postgres, Azure services, aggregator client, LLM calls).
      Adapters know the domain, not the other way around.
    - Generated models from `contractz` belong at the edge (adapters/REST layer), not in the domain model. Map explicitly
      between contract DTOs and domain models — no leaking contract types into the core.

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

> The commands below are the Maven standard for a multi-module reactor — verify against the root `pom.xml` and adjust once
> module names are confirmed.

```
mvn clean install                 # full build incl. contract codegen
mvn -pl contractz -am generate-sources   # regenerate contract only
mvn test                          # unit tests
mvn -pl api test                  # tests for a single module
```

Use Maven/the linter for style and compile errors — not Claude as a linter. Run existing tests/checks yourself via bash
rather than relying on your own judgment of correctness.

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