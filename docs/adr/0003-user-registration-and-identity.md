# ADR 0003 — User registration and identity in the backend

- **Status:** Accepted
- **Date:** 2026-10-09

## Context

Epic 3 of the build order: accounts and login, so that vacancies belong to a company and company data can feed the
generated vacancy texts. This ADR covers the first slice, registration: a recruiter registers with their work
e-mail, confirms it through a link in an e-mail and sets a password. Login, token issuing and securing the existing
endpoints are the next slice; they are designed here only far enough to make sure registration does not block them.

Requirements and constraints:

- **The backend owns identity.** User data is sensitive and Jobzy is heading towards enterprise customers; the
  user store, the credentials and the registration rules live in this repository, not in the frontend stack.
- **Spring Security, the current way**: stateless, JWT-based, `oauth2ResourceServer` style; no sessions.
- **Tokens carry IDs and rights only** (`sub`, `org`, `roles`). Name and e-mail are personal data and do not belong
  in something that ends up in every request log; the frontend fetches them from a `GET /me`.
- **Company comes from the e-mail domain.** The first person from a domain creates the company and becomes its
  owner; later registrants from the same domain join the existing company after the owner approves them. Public
  mail domains (gmail, outlook, proton, …) are not work domains and are rejected.
- **SSO later**, both social login and, for enterprise customers, per-organisation SAML/OIDC. Nothing of that is
  built now, but the model must not make it hard.
- **GDPR**: personal data and process data are separated per field, like everywhere in the code base.
- **EU only**: no external service outside the EU, including for mail or password-breach checks.

## Decision

### 1. Build identity in Spring, not with a third-party auth framework

**Better Auth was considered and rejected.** It is a TypeScript framework: it owns the `user`, `account`, `session`
and `verification` tables in a Node application, which would put identity with the frontend team and leave the
backend with a mirror of each user. Our registration flow (name and e-mail first, password only after the e-mail is
confirmed) is not its default flow and would be plugin work in Node. The hybrid (Better Auth issues JWTs, Spring only
validates) stays a valid option if the frontend stack ever needs it, because the backend validates JWTs the same way
either way.

**A self-hosted identity provider (Zitadel, Keycloak) was considered and deferred.** It is the strongest option for
per-customer enterprise SSO, but it is an extra container and database to run before there is a single customer.
The `User` model keeps credentials in a separate entity so that an external identity can sit next to, or instead of,
a password later, which is what a move to an IdP needs.

**Spring Authorization Server was considered and deferred** for the same reason: a full OAuth2/OIDC server is more
than a single SPA needs. The backend will issue its own JWTs with Spring Security's Nimbus support (next slice).

### 2. Domain model

Two aggregates in `domain/identity/`:

`Company`

| Field | Classification | Notes |
|-------|----------------|-------|
| `id` | – | UUIDv7. |
| `name` | `@ProcessData` | Given by the first registrant. |
| `emailDomain` | `@ProcessData` | Unique, lower-case. The key for "same company". |

`User`

| Field | Classification | Notes |
|-------|----------------|-------|
| `id` | – | UUIDv7; becomes the `sub` claim. |
| `firstName`, `lastName`, `email` | `@PersonalData` | `email` unique, lower-case. |
| `jobRole` | `@ProcessData` | Enum from the registration dropdown: `RECRUITER`, `HIRING_MANAGER`, `HR`, `FOUNDER_OR_DIRECTOR`, `OTHER`. A profile field, not a right. Classified as process data (maintainer decision): the values are too generic to identify a person on their own. |
| `companyId` | `@ProcessData` | |
| `membership` | `@ProcessData` | `OWNER` or `MEMBER`; derived, never chosen in a form. First user of a company is `OWNER`. |
| `status` | `@ProcessData` | `PENDING_VERIFICATION` → `ACTIVE` (first user of the company) or `PENDING_APPROVAL` (later users) → `ACTIVE` once the owner approves. |
| `credentials` | `@PersonalData` | Separate entity: Argon2id password hash, set at confirmation. Separate so an external identity (SSO) can be added without touching the user row. |
| `verificationToken` | `@ProcessData` | SHA-256 of the token and its expiry; cleared on confirmation. |

Rules the aggregates own:

- A `User` can only be confirmed while `PENDING_VERIFICATION` and before the token expires; the token is single-use.
- Membership and status are decided by whether the company already existed when the user registered.
- Owner approval of a `PENDING_APPROVAL` user needs an authenticated owner and is therefore part of the login slice;
  the status exists now so those registrations are not lost.

### 3. Registration flow

1. `POST /registration` with `firstName`, `lastName`, `email`, `jobRole`, `companyName`.
   - `email` must be syntactically valid and its domain must not be on the public-domain block list
     (`identity/public-email-domains.txt` on the classpath; extendable without code) → otherwise `400` on `email`.
   - Domain unknown → create `Company(name = companyName, emailDomain)`, user becomes `OWNER`.
   - Domain known → user joins that company as `MEMBER`; `companyName` is ignored.
   - Always `202`, also when the e-mail is already registered: an existing unconfirmed registration gets a fresh token
     and a new verification mail, a confirmed one gets a mail saying the account exists and inviting to log in. The
     response never reveals whether an e-mail address exists (enumeration). That the *company* exists is revealed (status `PENDING_APPROVAL` after confirmation); accepted.
   - A verification mail is sent through the `RegistrationMailer` port with a link to
     `jobzy.identity.confirmation-url` plus the token.
2. `POST /registration/confirm` with `token` and `password`.
   - Password 12–128 characters, no composition rules (NIST 800-63B), stored as Argon2id. No breach-list check: it
     would be an external call per registration and the usual service is outside the EU.
   - Valid token → `ACTIVE` for an owner, `PENDING_APPROVAL` for a member; token cleared. Invalid or expired → `400`.
3. A `@Scheduled` purge deletes users still `PENDING_VERIFICATION` after `jobzy.identity.unconfirmed-retention`
   (default `PT48H`), with their company if it has no other users. Same pattern as the generation purge in ADR 0002.

The mail adapter for now is `LoggingRegistrationMailer` (writes the link to the log). An EU mail provider goes behind
the same port later; that is the point where recurring cost starts.

### 4. Security configuration

Nothing is secured in this slice; every endpoint stays open, as today. The login slice adds `SecurityFilterChain`
with `oauth2ResourceServer().jwt()`, backend-issued JWTs (short-lived access token with `sub`, `org`, `roles`; refresh
token in an http-only cookie), `GET /me`, the owner-approval endpoint and the closing of `/vacancy`. Social login is
then `oauth2Login()` configuration; per-customer enterprise SSO is the trigger to revisit an IdP.

### 5. Contract

A separate `IdentityApi.yml` next to `VacancyApi.yml`, generated into `app.jobzy.api.identity.adapter...` by a
second execution of the generator plugin. Identity has a different consumer lifecycle and, soon, different security
than the vacancy API. The existing `bearerAuth` scheme in `VacancyApi.yml` stays as it is.

## Consequences

- New tables: `company`, `app_user` (`user` is reserved in SQL Server), `user_credentials`. `schema-change` label.
- A second codegen execution in `jobzy-api/pom.xml` (`<build>` change → `guardrail-change-ok`).
- `spring-boot-starter-security` enters the dependency tree now (Argon2 comes with it). The starter secures everything
  by default, so this slice ships a `SecurityFilterChain` that explicitly permits all requests and disables CSRF and
  form login, so behaviour does not change before the login slice. Having the chain in place now means the login
  slice only tightens configuration and the tests already run through the security filters.
- New configuration: `jobzy.identity.confirmation-url`, `jobzy.identity.unconfirmed-retention`.
- The frontend gets a registration form, a confirmation page that reads the token from the URL, and must treat
  `202` as "check your mail" regardless of whether the address was new.
- Delivered as one PR for registration and confirmation (contract, domain, persistence, use cases, mail port, purge,
  docs); login and securing endpoints are a separate PR series with its own ADR if the design here needs revision.
