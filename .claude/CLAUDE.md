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
- Keep recurring costs (cloud hosting, external APIs like the aggregator in epic 4) low — flag it if a change affects that.

## Tech stack

- **Java 25**, **Spring Boot 4.1.x** (Spring Framework 7, Jackson 3 under `tools.jackson.*`). Boot 4 has Java 17 as its minimum baseline but first-class support
  for 25 (JSpecify null-safety, modular jars) — use those features where it makes sense, but check library compatibility (not
  every Spring dependency is equally far along with Java 25/virtual threads).
- **Maven** as build tool, multi-module reactor, pinned via the Maven Wrapper (`./mvnw`, version and checksum in
  `.mvn/wrapper/maven-wrapper.properties`).
- **SQL Server** as database (`mssql-jdbc`); tests run on in-memory H2 (`test` profile).
- **Lombok** and **MapStruct** as annotation processors; **ArchUnit** for architecture tests; **RestAssured** for
  integration tests.
- **Cloud provider: not decided yet (Azure or AWS)**; custom domain jobzy.app. Keep the application cloud-agnostic:
  configuration through Spring properties / environment variables, a standard container image, and no Azure or AWS SDK
  outside an adapter behind a port. Flag any change that would tie the code to one provider.


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
    - Codegen is configured in `jobzy-api/pom.xml` (`openapi-generator-maven-plugin`, bound to `generate-sources`, output
      in `jobzy-api/target/generated-sources/openapi`). Generated code lands at the adapter edge, never under `domain`:
      models in `app.jobzy.api.vacancy.adapter.in.web.contract`, API interfaces in `app.jobzy.api.vacancy.adapter.in.rest`.
      A generated model landing under a `domain` package is a bug, not a style nit.
    - Note: generated packages are aggregate-first (`app.jobzy.api.vacancy.adapter...`), hand-written code is layer-first
      (see below). Don't "fix" this inconsistency as a side effect of another change.
- **`jobzy-api`** — the application core, built following **DDD + Hexagonal (Ports & Adapters)**. Hand-written code is
  organised layer-first under `app.jobzy.api`:
    - `domain/<aggregate>/` — entities, value objects (`domain/<aggregate>/valueobject/`), domain services, domain
      events. Shared domain base types (`BaseObject`, `UuidV7Generator`) live directly in `domain/`. **Zero** framework
      dependencies, no Spring annotations.
    - `application/service/` — use-case orchestration and transactions.
    - `application/port/in/` (with `application/port/in/command/` for inbound command DTOs) and `application/port/out/`
      — ports owned by the core.
    - `adapter/in/rest/` — inbound REST adapter: cross-aggregate `GlobalExceptionHandler` at the root,
      `adapter/in/rest/<aggregate>/` per aggregate with `mapper/request/`, `mapper/response/` and `validation/`
      subpackages for explicit contract-DTO ↔ domain mapping.
    - `adapter/out/persistence/` — outbound JPA adapter (SQL Server): `BaseJpaEntity` and JPA auditing config at the root,
      `adapter/out/persistence/<aggregate>/` per aggregate with its own `mapper/` subpackage.
    - `shared/` — cross-cutting code: `shared/config/` (e.g. `WebConfig`), `shared/exception/` (e.g. `BaseException`).
    - Adapters know the domain, never the reverse. Generated contract models belong at the `adapter/in` edge, not in the
      domain model — map explicitly between contract DTOs and domain models, never leak contract types into `domain` or
      `application`. `ArchitectureTest` enforces the layer rules.

## Architecture principles for changes

- Apply tactical DDD where the domain is interesting (matching, scoring, screening, retention/anonymization — see epic 2, an
  explicit learning hook for domain modeling). For CRUD-ish edges: no dogma, just be pragmatic.
- New external integrations (aggregator, LLM providers) always go behind a port with an adapter — never inject the SDK/client
  directly into a use case.
- For an architecture decision with real trade-offs: capture it as a short ADR in `docs/adr/` (`NNNN-kebab-title.md`)
  instead of deciding it in code alone. A revised ADR keeps the superseded alternative and why it was rejected.
- GDPR/personal data: process data (events, channel source, rejection reason) and personal data (name, CV, email) are
  deliberately separated in the data model so anonymization can wipe the latter while leaving the former intact. New
  candidate-related fields: decide explicitly which category they belong to before adding them.

## Build & test

Always use the wrapper (`./mvnw`), never a globally installed `mvn`.

```
./mvnw clean install                           # full build incl. contract codegen, tests and checks
./mvnw verify                                  # compile, all tests, format check — the definition of "green"
./mvnw -pl jobzy-contracts -am generate-sources   # regenerate contract only
./mvnw -pl jobzy-api test                      # tests for the api module only (fast inner loop)
./mvnw spotless:apply                          # fix formatting (google-java-format)
```

Use Maven/the linter for style and compile errors — not Claude as a linter. Run existing tests/checks yourself via bash
rather than relying on your own judgment of correctness. `./mvnw clean install` is not cheap — run it once per
verification pass, not repeatedly "to be sure"; only re-run it if you have a concrete new reason to suspect flakiness (a
fresh change to test isolation/config), not because an area was flaky once before.

## Guardrails

Deterministic checks are the source of truth for "does it work" — not an agent's own judgment. The rationale and the
roadmap of guardrails are in `docs/adr/0001-agentic-development-guardrails.md`.

- **Done means `./mvnw verify` is green.** Never report a task as done on a red or unrun build.
- **Never weaken a check to get green.** No skipping tests or checks (`-DskipTests`, `-Dmaven.test.skip`,
  `-D*.skip=true`, `--no-verify`), no `@Disabled`, no new `@SuppressWarnings`, no deleting or loosening assertions, no
  editing `ArchitectureTest` to make a violation pass. If a check is wrong, say so and stop — the human decides.
- **Formatting is checked, not auto-applied** by the build (`spotless:check` in `verify`). Run `./mvnw spotless:apply`
  before finishing.
- **Agents never merge, force-push, or label PRs.** Agents push branches and open PRs; only the human maintainer merges
  and applies override labels. `gh pr merge` is denied in `.claude/settings.json`.
- **Keep PRs reviewable**: aim for at most ~400 changed lines of production code and config per PR. Split larger work
  into a sequence of PRs that each leave the build green.
- **Changes to guardrails are changes to the safety net** (`.github/`, `.claude/`, `.agents/skills/`,
  `skills-lock.json`, `ArchitectureTest`, build plugin config): call them out explicitly in the PR description, never
  bundle them silently with feature work.
- **Third-party skills** (`.agents/skills/`, pinned by hash in `skills-lock.json`) are instructions agents execute —
  review an update like a dependency bump.

### Agent hooks (`.claude/hooks/`, wired in `.claude/settings.json`)

| Hook | Effect |
|------|--------|
| `guard-bash.sh` (PreToolUse, Bash) | **Blocks** skip/ignore flags (`-D…skip`, `-D…ignore`, `--fail-never`), `--no-verify` / `commit -n` / `core.hooksPath`, force/mirror/delete pushes and pushes to `main`, `reset --hard`, `clean -f`, discarding the whole tree, `gh pr merge`, label changes, branch-protection and repo-setting changes. **Asks** before a shell command writes to guardrail files. |
| `guard-paths.sh` (PreToolUse, Edit/Write) | **Blocks** edits to `target/` and generated sources. **Asks** before editing `.claude/`, `.github/`, `.githooks/`, `.agents/`, `.mvn/`, `mvnw`, `skills-lock.json` or `ArchitectureTest.java`. |
| `verify-on-stop.sh` (Stop) | Runs `./mvnw spotless:apply` + `./mvnw verify` when build-relevant files changed since the last green run and **blocks finishing while red** (3 attempts, then the human is warned). State and log in `.git/claude-guard/`. |

- When a hook blocks you, fix the cause — never work around a hook (e.g. by moving the command into a script file).
  If the hook is wrong, stop and tell the human.
- After changing a hook, run `.claude/hooks/test-hooks.sh` and extend it with a case for the change.
- `CLAUDE_SKIP_VERIFY_ON_STOP=1` (set by the human when starting Claude Code) disables the Stop hook for a session.

### CI (`.github/workflows/`) — the authoritative gate

| Job | Checks |
|-----|--------|
| `build` | `./mvnw verify` (compile, tests, ArchUnit, format check), CycloneDX SBOM, OSV-Scanner vulnerability scan of the SBOM |
| `checks` | gitleaks over the full history, actionlint and shellcheck, `.claude/hooks/test-hooks.sh` and `.github/scripts/test-guardrail-diff.sh`, `oasdiff` breaking-change check of the contracts against `main` (PRs only) |
| `guardrail-diff` (own workflow, PRs only) | `.github/scripts/guardrail-diff.sh` compares the PR with `main`; each finding fails unless the maintainer applies its override label (table below) |

- All three jobs must be green to merge into `main`. A red CI is never fixed by weakening a check.
- `guardrail-diff` runs on `pull_request_target`: the script from `main` judges the PR, so editing the script in a PR
  does not change the verdict on that PR. After changing it, run `.github/scripts/test-guardrail-diff.sh` and extend it
  with a case for the change.
- **Known vulnerability**: upgrade the dependency. If Spring Boot doesn't manage a fixed version yet, pin it under
  "security overrides" in the root `pom.xml` (ahead of the Spring Boot BOM) with the advisory IDs, and remove the pin
  once Spring Boot catches up. Never ignore a vulnerability without the human.
- **Breaking contract change**: make it backwards compatible (e.g. add optional fields, deprecate before removing). An
  intended break is the human's decision.
- Actions are pinned by commit SHA, downloaded tools by SHA-256 (`env` in `ci.yml`). Bump version and hash together.
  Dependabot opens weekly grouped update PRs for Maven dependencies and GitHub Actions.

| `guardrail-diff` finding | Override label (human-only) |
|--------------------------|-----------------------------|
| More suppressions than `main` (`@SuppressWarnings`, `@Disabled`, assumptions, `NOSONAR`, `spotless:off`, `<skip…>true`, `skip…=true`, `continue-on-error: true`, …) | `suppression-ok` |
| Fewer test methods than `main`, or a test class deleted | `test-removal-ok` |
| More than 400 changed lines of production code and config, or 800 of tests (docs and `.agents/` excluded) | `large-pr-ok` |
| Guardrail files changed (`.github/`, `.claude/`, `.githooks/`, `.agents/`, `.mvn/`, `mvnw`, `skills-lock.json`, `ArchitectureTest`, a `<build>` section in a `pom.xml`) | `guardrail-change-ok` |
| JPA entity (`@Entity`, `@Embeddable`, `@MappedSuperclass`) or `ddl-auto` changed | `schema-change` |
| `CLAUDE.md` names a path that does not exist | none — fix `CLAUDE.md` |

- An agent fixes the cause of a finding; when the finding is intended, it says so in the PR description and leaves the
  label to the maintainer.

### One-time setup per clone (human)

```
brew install gitleaks
ln -s ../../.githooks/pre-commit .git/hooks/pre-commit   # secret scan on every commit
```

## Language policy (strict)

**Everything in this codebase is in English.** No exceptions:

- **Code**: identifiers, method/class/variable names, log messages, exception messages.
- **Javadoc and comments**: English only.
- **Commit messages and PR descriptions**: English only.
- **ADRs and in-repo docs**: English only.

Dutch is fine in conversation with the human developer but never leaks into anything that ends up in the repository.

## Working style in this repo

- Be critical and direct in code review and proposals — no cheerleading. If an approach is weak or lets scope creep, say so.
- When torn between the "clean/academic variant" and the "pragmatic variant": name both with trade-offs, make the choice
  explicit.
