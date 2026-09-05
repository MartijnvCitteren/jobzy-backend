# Jobzy backend agent team — learnings

Retro notes appended by jobzy-scrummaster after each completed task/epic. Every subagent
on this team reads this file before starting work — keep entries concrete and actionable.

## 2026-09-05 — GitHub issue #79, "Write vacancytext manually"

**What worked**
- Single trailing `[review-gate]` task (plan Task 12) instead of per-task review kept the developer
  moving TDD through all 11 tasks back-to-back without interruption, and the reviewer still caught a
  real bug: they specifically checked that the upsert-by-`vacancyId` logic in
  `VacancyRepositoryAdapter` doesn't silently accumulate duplicate `VacancyDescriptionJpaEntity` rows
  on a second write — the exact failure mode the plan's own risk #5 called out in advance. Naming a
  known-risky spot in the plan up front, then having the reviewer verify it specifically rather than
  trusting the diff, is worth repeating.
- Reviewer verified a new pom dependency by running `mvn dependency:tree` rather than trusting the
  developer's description of it — don't take a teammate's claim about dependency shape on faith when a
  one-command check is cheap.
- The architect's plan surfaced two real pre-existing bugs while researching the feature, unrelated to
  #79 itself (`Vacancy.Builder` had no way to reconstruct an existing id, blocking any future
  `findById`; `GlobalExceptionHandler` had no 404/generic-500 handling, which also silently affected
  the existing `CreateVacancyService`'s DB-failure path) — both were fixed as explicit, separately
  justified prerequisite tasks (ADR 0004 / plan Task 1, Task 7) rather than folded silently into the
  main change. Surfacing "found while researching, fixing as a named prerequisite" is a good pattern:
  it keeps the fix visible and reviewable instead of being scope creep buried in an unrelated diff.
- ADR 0004 kept both the architect's original recommendation and the human's overriding decision, with
  the rejected alternative's reasoning still written out (Decisions 1 and 3). When the human later
  needs to revisit the trade-off, the "why we didn't do the textbook version" is still on record instead
  of having been silently deleted. Worth reinforcing as the standard: a revised ADR should read as
  "decision + superseded alternative + why," never as a quiet rewrite.

**What wasted tokens or time**
- Two architects (`jobzy-architect-79` and `jobzy-architect-79-2`, the latter on a different model)
  were run on the same task at the same time. The team lead saw the first architect go idle
  mid–`mvn test` and, on a user request to switch models, spawned a fresh duplicate agent with the full
  original prompt instead of resuming/checking the first one — the first agent wasn't actually stuck,
  it was just idle-notified at a normal tool-call boundary. The second agent found the plan/ADR files
  already on disk from the first and re-derived the same conclusions before being told to stand down.
  Net effect: a full duplicate planning pass burned for no benefit (the same plan and ADR were produced
  twice).
- **Fix**: before spawning a replacement agent for one that reports idle/interrupted, send it a message
  first and check whether it responds or is genuinely blocked (e.g. on a permission prompt). Only spawn
  a fresh duplicate if that check confirms it's actually stuck — being idle right after a tool call is
  the normal state between turns, not evidence of being blocked. If a model switch is genuinely needed
  for an agent that's fine, prefer resuming the same agent under the new model/config where the harness
  allows it, rather than a full duplicate from scratch.

**What to change**
- **Environment/infra gap, not a teammate behavior fix**: the team playbook
  (`.claude/commands/backend-team.md`) and the `require-review-signoff.sh` hook assume a real shared
  task-tracking tool (create/claim/complete task objects with `task_subject`/`task_description`) is
  available to the team lead, backend developer, and code reviewer. In this run no such tool was ever
  available — the "task list" was only prose in `.claude/planning/79-write-vacancytext-manually.md`,
  and the review sign-off ended up being recorded by hand (team lead directly edited the plan file to
  add "Reviewed-by: jobzy-code-reviewer" under Task 12, as seen in this file's Task 12 status line)
  because there was nothing to formally mark complete. This doesn't need a workaround from future
  teammates — it needs whoever configures agent teams/hooks in this environment to either provision the
  expected task-tracking tool or update the playbook and hook to match the prose-plan-file reality.
  Flagging here so it isn't silently rediscovered and hand-patched the same way on every future run.
- When an idle-notification comes in for a teammate mid-verification-run (e.g. mid `mvn test`), treat it
  as expected rather than reflexively escalating to a duplicate agent spawn — see the fix above.
