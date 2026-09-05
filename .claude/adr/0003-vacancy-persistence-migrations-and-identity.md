# ADR 0003 — Vacancy persistence: JPA, schema migrations, identity, and repository tests

**Status**: Accepted (2026-08-02, by the human developer — §3 decided against the architect's
recommendation, see below)

**Date**: 2026-08-02

**Context feature**: `specs/001-create-vacancy`

## Context

`jobzy-api` has **no persistence stack on the classpath**. Its declared dependencies are
`jobzy-contracts`, `spring-boot-starter-web`, `spring-boot-starter-validation`,
`swagger-annotations-jakarta`, `mapstruct` and `spring-boot-starter-test`. There is no
`spring-boot-starter-data-jpa`, no JDBC driver, and no migration tool.

The configuration, however, already anticipates one: `application.yml` sets
`spring.jpa.hibernate.ddl-auto: update` and `show-sql: true`, and `application-local.yml` names
`com.microsoft.sqlserver.jdbc.SQLServerDriver` with a `SQLServerDialect`. So the intended target is
Azure SQL Server, but nothing wires it up.

Create Vacancy cannot ship without storage: FR-002 (assign identifier and persist), FR-005 (no
partial record on store failure), FR-007 (return the identifier) and SC-004 (immediately retrievable)
all depend on it, and User Story 3 is *entirely* about data-store failure behaviour.

Three coupled questions fall out, plus one about identity that is easy to get wrong.

## Decision

### 1. ORM and driver

Add `spring-boot-starter-data-jpa` and `com.microsoft.sqlserver:mssql-jdbc`. JPA/Hibernate is what
the existing configuration already assumes, and Vacancy is a simple aggregate with no query
complexity that would justify jOOQ or JDBC-by-hand. No trade-off worth arguing here.

### 2. Schema management — Flyway, replacing `ddl-auto: update`

Add `flyway-core` plus `flyway-sqlserver`, create
`jobzy-api/src/main/resources/db/migration/V1__create_vacancy.sql`, and change `ddl-auto` to
`validate`.

- *Pragmatic-looking variant*: keep `ddl-auto: update`. Zero setup, schema follows the entities.
- *Why it is rejected*: `update` never drops or alters incompatibly, so the schema silently diverges
  from what the entities claim; there is no reviewable history of schema change; and there is no safe
  path to change a column in a deployed environment. It is a liability the moment the first
  environment holds data anyone cares about — which for an ATS is immediately. `validate` plus
  explicit migrations costs one SQL file per change and makes schema change a reviewable diff.

This is the cheapest moment to make this call: there is no schema and no data yet. Doing it later
means writing a baseline migration against whatever Hibernate happened to generate.

### 3. Repository tests — H2 in-memory (DECIDED AGAINST THE ARCHITECT'S RECOMMENDATION)

The unit-testing skill mandates `@DataJpaTest` for repositories, which needs a database.

**Decision: H2 in-memory, in SQL Server compatibility mode.** Add test-scoped `com.h2database:h2`.
No Testcontainers, no Docker.

```
jdbc:h2:mem:jobzy;MODE=MSSQLServer;DB_CLOSE_DELAY=-1
```

**Rationale (the human's, and it is sound):** there is **no CI configuration anywhere in this
repository** and Docker is not running on the development machine. Recommending Testcontainers would
have made the entire test suite depend on infrastructure that does not exist and cannot currently be
verified to work. An unverifiable dependency in the one place the project relies on for correctness
(Principle VII: verify with the project's own tools) is a worse risk than an imperfect test database.
Principle V applies: take the route that keeps momentum.

**Flyway runs against H2 in tests too**, rather than letting Hibernate generate the test schema with
`create-drop`. This matters: if tests build their schema from the entities while production builds it
from migrations, drift between the two becomes structurally invisible, which defeats the point of
choosing Flyway in §2. Consequence: **`V1__create_vacancy.sql` must be written in a conservative,
portable SQL subset** that parses on both H2/MSSQLServer-mode and real Azure SQL Server. That is a
real constraint on how the migration is written, not a detail.

#### Accepted cost, recorded explicitly

H2 in SQL Server compatibility mode is an approximation, not the real engine. The following are known
divergences that these tests will *not* catch, accepted knowingly:

- **Type mapping.** `uniqueidentifier` is emulated; H2's UUID handling is not byte-for-byte SQL
  Server's. Column-type surprises will first appear on deployment, not in the suite.
- **Error codes and exception translation.** Constraint-violation and connectivity failures surface
  with different SQLState/vendor codes, so Spring's `DataAccessException` subtype may differ from
  production. **Consequence for T015/T017: assert on `DataAccessException` (the base type), never on
  a specific subtype or a SQL Server error code** — such an assertion would pass against H2 and be
  wrong in production.
- **Ordering of `uniqueidentifier`.** SQL Server's native collation of `uniqueidentifier` is *not*
  byte order. H2 will not reproduce that, so the UUIDv7 ordering caveat in §4 below is guaranteed not
  to be caught by any test here.
- **DDL dialect.** Anything SQL Server-specific in the migration will fail at test time — which is
  the constraint noted above, and is at least a loud failure rather than a silent one.

**Mitigation**: the first real deployment to Azure SQL is the actual verification of the schema. When
CI does exist, a periodic or pre-release job running the migration against a real SQL Server instance
would close this gap cheaply. Worth revisiting this ADR at that point.

*(For the record: the architect recommended Testcontainers with `org.testcontainers:mssqlserver`, on
the grounds that tests passing against an approximation are worse than no tests. The human's Docker
availability argument outweighed it. The divergences above are the price, and they are documented so
that a future failure in production is recognised as a known gap rather than a mystery.)*

### 4. Identity: UUIDv7, assigned by the core, not by Hibernate

The contract types `VacancyId` as `string/uuid`, and `VacancyApi.yml` explicitly states that
`listVacancies` "uses keyset/cursor pagination on `id` (UUIDv7, time-ordered)". So **UUIDv7 is a
contractual requirement**, not a preference — a random UUIDv4 would silently break cursor pagination
the moment that endpoint is built, and would do so invisibly, since v4 ids sort fine, just in
meaningless order.

**Decision**: the `Vacancy` aggregate receives its `VacancyId` at construction, generated in the
core, *before* it reaches the persistence adapter.

- *Alternative rejected*: let Hibernate assign it via `@UuidGenerator(style = Style.TIME)` on the
  entity. Zero extra dependency, but the aggregate then has no identity until it has been saved,
  which means the domain object is not fully-formed until infrastructure touches it, and the use case
  cannot log or reason about the vacancy it just built. For User Story 3 in particular — "no partial
  record, and the id is returned on success" — having identity before persistence keeps the flow
  simple and lets `CreateVacancyService` be tested with a mocked port and no database at all.
- Java's `java.util.UUID` has no UUIDv7 factory, so generation needs either
  `com.fasterxml.uuid:java-uuid-generator` (`Generators.timeBasedEpochGenerator()`) or a small
  hand-rolled generator. **Use the library** — hand-rolling a spec'd id format to save one small,
  widely-used dependency is a poor trade.
- Persist as the SQL Server `uniqueidentifier` type, and be aware that SQL Server's native ordering of
  `uniqueidentifier` is *not* byte order — if the cursor-pagination feature later sorts in SQL, it
  must order on an explicit column, not rely on the id's native collation. Flagged here so the
  listing feature does not rediscover it painfully. Per §3, no test in this suite will catch it.

### 5. Transactional integrity for User Story 3

`CreateVacancyService.createCoreVacancy` is annotated `@Transactional`. A single-aggregate insert is
atomic by definition, so FR-005 is satisfied by the database, not by application logic — the
important part is that the exception is allowed to propagate rather than being swallowed, and that it
surfaces as a 500 `ProblemDetails` (T015). The corresponding test asserts that a port failure
propagates and that nothing is persisted; it must not be written as a test of Spring's transaction
manager.

### 6. Category storage

Persist enums by name via `@Enumerated(EnumType.STRING)` — readable in the database, immune to
reordering, and matching the names already used on the wire. This supersedes the numeric
`categoryNumber` codes on `VacancyCategoryDto` unless the human confirms an external consumer depends
on them (see plan R5); if one does, that mapping belongs in the persistence adapter, not in an
inbound DTO.

## Consequences

- New dependencies: `spring-boot-starter-data-jpa`, `mssql-jdbc`, `flyway-core`, `flyway-sqlserver`,
  `java-uuid-generator`, and test-scoped `com.h2database:h2`. All OSS, no recurring cost.
- **No Docker requirement.** The full suite runs with `mvn test` on a bare machine, which is the
  point of the §3 decision.
- `V1__create_vacancy.sql` is constrained to portable SQL that parses on both H2/MSSQLServer-mode and
  Azure SQL Server.
- Failure-path tests assert on `DataAccessException` only, never on vendor error codes.
- `spring.jpa.hibernate.ddl-auto` changes from `update` to `validate`; every subsequent schema change
  needs a numbered migration file. This is deliberate friction.
- The `Vacancy` aggregate is fully-formed and identifiable before any infrastructure is involved,
  which keeps `CreateVacancyService` unit-testable without a database.
- A known, documented gap exists between what the test suite proves and what production will do. This
  ADR should be revisited once CI exists.
