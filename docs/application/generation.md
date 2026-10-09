# Vacancy description generation

A recruiter can let an LLM draft the description of an existing vacancy from three short answers (the most important
tasks, the team, why the job is nice). The draft is a suggestion: the frontend shows it for editing and the user saves
it through the manual endpoint (`POST /vacancy/{id}/description`, see `vacancy.md`). Nothing generated is ever written
to the vacancy automatically. The design and its rejected alternatives are in ADR 0002.

## Domain model

`VacancyDescriptionGeneration` (`domain/generation`) is its own aggregate, not part of `Vacancy`. It references the
vacancy by id only and never loads it.

| Field | Notes |
|-------|-------|
| `id` | UUIDv7; the `generationId` in the contract. |
| `vacancyId` | The vacancy it was started for. |
| `input` | `GenerationInput`: snapshot of the vacancy's job title and language plus the three answers. |
| `status` | `PENDING` → `COMPLETED` or `FAILED`. |
| `failureReason` | `PROVIDER_ERROR`, `TIMEOUT`, `INPUT_REJECTED`, `INVALID_OUTPUT`; only when `FAILED`. |
| `draft` | `GeneratedDraft(summary, jobDescription, tasks)`; only when `COMPLETED`. `whatWeOffer` and `aboutUs` are never generated: there are no facts for them until company data exists. |
| `model`, `promptVersion` | What produced the draft. Also kept when a draft is rejected as invalid. |
| `requestedAt`, `completedAt` | `completedAt` is set in either final state. |

Rules the aggregate owns:

- It ends exactly once; a second `acceptDraft` or `fail` throws.
- **90-second timeout.** A `PENDING` generation older than `PENDING_TIMEOUT` is reported as `FAILED` / `TIMEOUT` by
  `observedAt(now)`, which the poll uses. This covers a crash or restart during generation; the stored row stays
  `PENDING` until the retention clean-up removes it. A result that arrives after the deadline is recorded as `TIMEOUT`
  too, so the outcome never flips after a poll reported it.
- **A draft must be saveable.** `acceptDraft` ends in `FAILED` / `INVALID_OUTPUT` unless every text is non-blank, within
  the contract limits (1000 / 5000 / 5000) and passes `TextContentRules` (no raw tags, no control characters other than
  newline, carriage return and tab), the same rules as user input.
- Starting a new generation while another is `PENDING` is allowed; the user polls the newest.

## Flow

```
POST /vacancy/{id}/generate-description
  VacancyDescriptionGenerationController
    1. bean validation (maxLength 1000, all three required)
    2. GenerateVacancyDescriptionRequestValidator (TextContentRules) → 400 naming the field
  StartVacancyDescriptionGenerationService  (@Transactional)
    loads the vacancy (404), snapshots job title + language, saves PENDING,
    publishes VacancyDescriptionGenerationStarted
  ← 202 {generationId, status: PENDING}, Location: …/generate-description/{generationId}

after commit:
  VacancyDescriptionGenerationStartedListener  (@TransactionalEventListener AFTER_COMMIT + @Async, virtual thread)
  CompleteVacancyDescriptionGenerationService
    load PENDING generation      (REQUIRES_NEW transaction)
    VacancyDescriptionGenerator  (no transaction: no DB connection held while waiting on the provider)
    acceptDraft / fail, save     (REQUIRES_NEW transaction)

GET /vacancy/{id}/generate-description/{generationId}
  GetVacancyDescriptionGenerationService: find by id and vacancy (404 otherwise), apply the 90-second rule
  ← 200 PENDING | 200 COMPLETED with description | 422 INPUT_REJECTED | 500 PROVIDER_ERROR, TIMEOUT, INVALID_OUTPUT
```

- `AFTER_COMMIT` guarantees the `PENDING` row exists before work starts, so a poll never sees a `404` for a generation
  that was just started.
- The completion uses `REQUIRES_NEW` because in an `AFTER_COMMIT` callback the committed transaction is still bound to
  the thread when the executor is synchronous (as in tests); joining it would lose the writes.
- If the generator throws despite its contract, the generation is recorded as `PROVIDER_ERROR` rather than left
  `PENDING`.
- A generation is only readable through the vacancy it was started for.
- State lives in the database, so a poll may hit another instance. The background work itself is in-process: a crash
  loses it, and the 90-second rule turns that into a `TIMEOUT`.

## The generator port and the Mistral adapter

`VacancyDescriptionGenerator` (`application/port/out`) takes a `GenerationInput` and returns a `GenerationResult`:
`Drafted(draft, model, promptVersion)` or `Rejected(reason)`. It never throws for a failed generation and never sees
Spring AI or contract types.

`MistralVacancyDescriptionGenerator` (`adapter/out/ai/mistral`) is the only implementation, using Spring AI's
`ChatClient` with the Mistral starter:

- **Configuration** (`application.yml`): `mistral-small-latest`, temperature 0.5, max 1000 output tokens; API key from
  `MISTRAL_API_KEY`. The recorded `model` is the version Mistral reports in the response (a dated snapshot), falling
  back to the configured alias.
- **Prompts** are classpath templates in `prompt/vacancy-description/v1/` (`system.st`, `user.st`). A prompt change is a
  new version directory in a reviewed PR; the version is stored on every generation.
- **Structured output**: the JSON schema of `MistralDraftResponse` (`summary`, `jobDescription`, `tasks`,
  `inputRelevant`, `rejectionReason`) is sent as a strict `json_schema` response format. `inputRelevant=false` becomes
  `INPUT_REJECTED`; a response that does not parse or misses a section becomes `INVALID_OUTPUT`. The model's
  `rejectionReason` is neither stored nor returned.
- **Prompt-injection defences**: one fixed task in one call without conversation; the answers only in the user message,
  each inside `[[[name]]] … [[[/name]]]` blocks that the system prompt declares to be data, with the brackets stripped
  from the answers first; a random canary token per call in the system prompt, and output containing it is
  `INVALID_OUTPUT`; the saveability rule above on every draft.
- **Timing**: per attempt 15 s read timeout and 5 s connect timeout (`spring.http.clients.*`, JDK HTTP client so a
  timeout is recognisable as `HttpTimeoutException`); 3 attempts in total with 1–2 s backoff (`spring.ai.retry.*`). 4xx
  responses are not retried. Worst case stays under one minute. A timeout on the last attempt is `TIMEOUT`, every other
  failure `PROVIDER_ERROR`.
- Nothing logs the prompt, the answers or the draft; `GenerationInput` and `GeneratedDraft` leave free text out of
  `toString()`.

## Retention

`GenerationRetentionJob` (`adapter/in/scheduling`) calls `PurgeExpiredGenerationsService` every
`jobzy.generation.purge-interval` (default `PT1H`, first run one minute after start). It bulk-deletes generations
requested longer ago than `jobzy.generation.retention` (default `PT32H`). It touches only the generation table. Every
instance runs it; the delete is idempotent. `jobzy.scheduling.enabled=false` switches all scheduled jobs off (tests do
this).

## Persistence

One table, `vacancy_description_generation` (`VacancyDescriptionGenerationJpaEntity`), with the input and draft
flattened into columns and an index on `requested_at` for the clean-up. `vacancy_id` is a plain column, not a foreign
key association.

- `@PersonalData`: the three answers and the three draft texts (free text about a team routinely names people, and the
  draft can repeat it).
- `@ProcessData`: everything else, including the job title and language snapshot (copies of vacancy fields, which are
  process data on the vacancy as well).

## Testing

- Integration tests (`BaseIntegrationTest`) replace the application task executor with a `SyncTaskExecutor`
  (`SynchronousAsyncConfig`) and mock the generator port, so a generation has finished when the `POST` returns. No
  sleeping or polling.
- `MistralVacancyDescriptionGeneratorTest` runs the adapter with the real Spring AI configuration against WireMock
  (`spring.ai.mistralai.chat.base-url`); only the timeout and backoff are shortened.
- The test profile points `spring.ai.mistralai.chat.base-url` at an unreachable address. Override the **chat** base
  URL: it defaults to `api.mistral.ai` and wins over `spring.ai.mistralai.base-url`.

## Known gaps

- No rate limiting or authentication on the endpoint (waits for login), so the application must not be exposed
  publicly with a real `MISTRAL_API_KEY`.
- The application does not start without `MISTRAL_API_KEY`; there is no stub generator for running without a key.
- No audit trail of generations beyond the 32-hour retention. Vacancy text is not a candidate decision; the audit trail
  is designed with candidate matching (epic 5).
- A crash during generation is only noticed through the 90-second rule; there is no retry of lost work.
