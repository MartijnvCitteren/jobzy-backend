# Identity

Accounts and companies. This page covers what is built today: registration with a work e-mail address and its
confirmation (ADR 0003). Login, tokens, `GET /me`, owner approval and securing the other endpoints do not exist yet.

## Domain model

Two aggregates in `domain/identity`, linked by id only (a user holds `companyId`, there is no JPA association).

`Company`

| Field | Notes |
|-------|-------|
| `id` | UUIDv7. |
| `name` | Given by the first registrant of the domain; trimmed, required. |
| `emailDomain` | Lower case, unique. The key for "same company". |

`User`

| Field | Notes |
|-------|-------|
| `id` | UUIDv7; will become the `sub` claim. |
| `firstName`, `lastName` | Trimmed, required (`Registrant`). |
| `email` | `EmailAddress`: trimmed, lower case, one `@`, a domain with a dot. |
| `jobRole` | `RECRUITER`, `HIRING_MANAGER`, `HR`, `FOUNDER_OR_DIRECTOR`, `OTHER`. A profile field, not a right. |
| `companyId` | The company of the e-mail domain. |
| `membership` | `OWNER` for the first user of a company, `MEMBER` after that. Derived, never chosen. |
| `status` | `PENDING_VERIFICATION` → `ACTIVE` (owner) or `PENDING_APPROVAL` (member). |
| `credentials` | `UserCredentials(passwordHash)`, Argon2id; `null` until confirmed. |
| `verificationToken` | `VerificationToken(tokenHash, expiresAt)`: SHA-256 of the raw token; `null` once confirmed. |

Rules the `User` aggregate owns:

- `register` decides membership from whether the company already existed and starts in `PENDING_VERIFICATION`.
- `confirm` only works while `PENDING_VERIFICATION`, with the matching token hash, before `expiresAt`. It sets the
  credentials, clears the token (single use) and moves to `ACTIVE` or `PENDING_APPROVAL`. Otherwise it throws
  `InvalidVerificationTokenException`, one message for unknown, used and expired tokens.
- `renewVerificationToken` replaces the token of an unconfirmed user; the old link stops working.

`Password` enforces 12 to 128 characters, counted as code points, with no composition rules (NIST SP 800-63B).
`toString` of `EmailAddress`, `Password`, `Registrant`, `UserCredentials`, `User` and the commands is redacted, so
personal data and secrets do not reach the logs.

## Implemented use cases

### Register — `POST /registration`

`RegisterUserService`:

1. Bean validation on the contract model: required fields, lengths, non-blank names, `@Email`. Then
   `EmailAddress` checks the shape the domain needs. A public mail domain (`PublicEmailDomains` port) is rejected.
   Both give `400` on `email`.
2. Address unknown: the domain's company is looked up. If there is none, `Company.found` creates it and the user
   becomes `OWNER`. Otherwise the user joins as `MEMBER` and `companyName` is ignored. A token is issued, the user
   is saved and `RegistrationMailer.sendVerificationMail` gets the raw token.
3. Address known and still unconfirmed: a fresh token replaces the old one and a new verification mail goes out.
   The name, job role and company name of the repeated request are ignored.
4. Address known and confirmed: `sendAccountExistsMail`, nothing is saved.

The response is always `202` with an empty body for a valid request, so it never reveals whether an address has an
account. That the company exists does leak, through `PENDING_APPROVAL` after confirmation; ADR 0003 accepts that.

`VerificationTokenIssuer` creates 32 bytes from `SecureRandom`, URL-safe Base64 without padding (43 characters).
Only the SHA-256 hash is stored. A token is valid for `jobzy.identity.unconfirmed-retention`.

### Confirm — `POST /registration/confirm`

`ConfirmRegistrationService` checks the password policy first and then the token, and only then hashes the password.
That way the slow Argon2 hash runs only for a request that will succeed. It returns `200` with
`{"status": "ACTIVE" | "PENDING_APPROVAL"}`. A policy violation gives `400` on `password`. An unknown, used or
expired token gives `400` on `token`.

### Purge unconfirmed registrations (scheduled)

`UnconfirmedRegistrationPurgeJob` (`adapter/in/scheduling`) calls `PurgeUnconfirmedRegistrationsService` every
`jobzy.identity.purge-interval`. The first run also waits one interval. The service deletes users still
`PENDING_VERIFICATION` whose token expired, and their company if no user is left. Because a repeated registration
renews the token, retention runs from the latest verification mail. Tests call the use case directly.

## Adapters

- **REST**: `RegistrationController` implements the generated `RegistrationApi`. `IdentityExceptionHandler`
  (highest precedence) maps the three identity exceptions to `400` problem details naming the field. Requests are
  not logged.
- **Mail**: `LoggingRegistrationMailer` writes the confirmation link to the log at INFO, built from
  `jobzy.identity.confirmation-url` plus `?token=…`. It names the user by id, never by e-mail address. An EU mail
  provider replaces it behind the same port; that is where recurring cost starts.
- **Password hashing**: `Argon2PasswordHasher` uses Spring Security's `Argon2PasswordEncoder` with the OWASP minimum:
  19 MiB memory, 2 iterations, parallelism 1, 16-byte salt, 32-byte hash. Argon2 needs BouncyCastle
  (`bcprov-jdk18on`), which Spring Boot does not manage; its version is pinned in the root `pom.xml`.
- **Block list**: `ClasspathPublicEmailDomains` reads `identity/public-email-domains.txt` once at startup. It has
  one domain per line and `#` comments. An entry like `hotmail.*` matches the name under any top-level domain. Extend
  the file to block more domains, no code needed.

## Persistence

| Table | Entity | Notes |
|-------|--------|-------|
| `company` | `CompanyJpaEntity` | `email_domain` unique. All fields `@ProcessData`. |
| `app_user` | `UserJpaEntity` | `user` is reserved in SQL Server. `email` unique. Indexes on `company_id` and `verification_token_hash`. |
| `user_credentials` | `UserCredentialsJpaEntity` | Shares the user's primary key (`@MapsId`), fetched eagerly, saved and deleted by cascade. |

- GDPR: `first_name`, `last_name`, `email` and `password_hash` are `@PersonalData`. `job_role`, `company_id`,
  `membership`, `status` and the token columns are `@ProcessData`, following the ADR 0003 table.
- The token hash has a plain index, not a unique constraint. SQL Server allows only one `NULL` in a unique column,
  and every confirmed user has a `NULL` hash.

## Contract

`jobzy-contracts/.../IdentityApi.yml`, generated by a second `openapi-generator-maven-plugin` execution into
`app.jobzy.api.identity.adapter.in.rest` (`RegistrationApi`) and `app.jobzy.api.identity.adapter.in.web.contract`. Both
operations declare `security: []`.

The execution differs from the vacancy one in two ways:

- **`schemaMappings` reuses the generated vacancy `ProblemDetails`.** The schema is identical, and
  `GlobalExceptionHandler` already answers every controller with that class. A second copy would also need its own
  SpotBugs exclusion, because the generated `List` accessors are flagged.
- **`generateSupportingFiles` is off.** Both executions write to the same output directory, so the identity run would
  overwrite the vacancy run's `ApiUtil` and `EnumConverterConfiguration`. Because of this the generated identity
  packages have no `package-info.java` with `@NullMarked`. A generated `ProblemDetailsErrorsInner` is left over and
  unused.

## Known gaps

- Spring Boot's `UserDetailsServiceAutoConfiguration` is still active. It creates an unused in-memory user and logs
  "Using generated security password" at startup. No login mechanism is enabled, so nothing accepts it. Remove it in
  the login slice.
- In production the confirmation link, which contains a working token, ends up in the application log until a real
  mailer replaces `LoggingRegistrationMailer`.
- The mail is sent inside the registration transaction. With a real mail provider, a failing send rolls back the
  registration (`500`), and a send followed by a failing commit mails a link that does not work.
- Two simultaneous first registrations for the same new domain hit the unique constraint on `email_domain`. The
  second one gets a `500`. The same applies to two simultaneous registrations of one address.
- There is no optimistic locking (`@Version`). Two simultaneous confirmations with the same token can both succeed,
  and the last password wins.
- Saving an already-confirmed user again would write `NULL` into `user_credentials.created_at`. The adapter maps a
  fresh credentials entity, and JPA auditing only fills `created_at` on insert. Today a confirmed user is never saved
  again. The vacancy description has the same pattern.
- A new address does more database work than an existing one, so response timing may hint at whether an address is
  registered. Not measured or mitigated.
- `PENDING_APPROVAL` users stay that way: owner approval needs login.
