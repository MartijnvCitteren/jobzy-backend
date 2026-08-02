---
description: Team-lead playbook for running the Jobzy backend agent team on a task or epic. Only run this from the main session -- it becomes the team lead the moment it spawns the first teammate.
argument-hint: [task or epic description]
---

You are about to act as team lead for the Jobzy backend agent team, working on:

$ARGUMENTS

You (the current session) become the lead automatically the moment you spawn the first
teammate — there is no separate "team lead" subagent to spawn. Agent teams require
`CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1`, already set in `.claude/settings.json`. You are
the only one who talks to the human directly; teammates report to you, and you relay
anything ambiguous back to the human instead of guessing on their behalf.

Read `.claude/team-learnings.md` before doing anything else — apply what past runs of
this team learned.

## 1. Speckit first

For anything beyond a trivial one-line fix, don't let any teammate touch code before a
finalized spec exists. Check whether spec-kit is already set up in this repo: look for a
`.specify/` directory and whether `/speckit.specify`, `/speckit.clarify`, and
`/speckit.plan` are available as commands.

- **If spec-kit is present**: facilitate `/speckit.specify` and then `/speckit.clarify`
  with the human yourself, in this conversation, before spawning anyone. Don't hand this
  step to a teammate — spec clarification is a conversation with the human, and you're
  the only one who has that conversation.
- **If spec-kit is missing**: stop and tell the human. Ask whether they want it
  installed before you proceed — do not install it yourself without that confirmation,
  and never install anything outside this project's scope without asking first.
- **Trivial fix exception**: if the task is genuinely small and unambiguous (a one-line
  bug fix, a typo, a config tweak), you may skip Speckit and go straight to spawning the
  backend developer for that single task. If in doubt, don't skip it — ask the human.

## 2. Architect: plan from the spec

Once a spec (and, ideally, a `/speckit.plan` / `tasks.md`) exists, spawn a teammate using
the `jobzy-architect` agent type. Have it:

- Read the spec and any existing plan output.
- Produce or review the `/speckit.plan` output for this codebase's actual module
  structure (`contractz` vs `api`).
- Break the work into the shared task list, with each task description referencing the
  corresponding Speckit task ID from `tasks.md` for traceability.

Review what it produces yourself before moving on. If it flags architectural risks or a
pragmatic-vs-academic trade-off, that decision may need the human — relay it rather than
picking one yourself.

## 3. Backend developer: implement, one task at a time

Spawn a teammate using the `jobzy-backend-developer` agent type. It works strictly
TDD, claiming or being assigned one task at a time from the shared list. Let it
self-claim if the task breakdown is clean; assign explicitly if some tasks need a
particular order.

## 4. Code reviewer: the only path to "done"

A task is not done until a teammate using the `jobzy-code-reviewer` agent type has
signed off on all three axes (coding standards, architecture, functionality). This is
enforced mechanically: the `TaskCompleted` hook
(`.claude/hooks/require-review-signoff.sh`) blocks marking a task complete unless its
description carries a `Reviewed-by: jobzy-code-reviewer` marker. Don't try to route
around this hook — if a teammate reports being blocked, that means review didn't happen
yet, not that the hook is wrong.

Spawn the reviewer once the backend developer reports a task's tests are green. For
unusually high-stakes changes (security, GDPR/personal-data handling, schema
migrations), consider spawning the reviewer with `model: opus` instead of its default
Sonnet.

## 5. Documentation writer: only after sign-off

Once a task carries the review sign-off marker, spawn a teammate using the
`jobzy-documentation-writer` agent type, scoped only to what changed in that task
(Javadoc, README/docs, `contractz` OpenAPI YAML). Never spawn it before review sign-off.

## 6. Retro before shutdown

Once the epic/task is finished, spawn a teammate using the `jobzy-scrummaster` agent
type for a short retro. It appends findings to `.claude/team-learnings.md`. Wait for it
to finish before shutting the team down.

## Working notes

- Give each teammate a clear, memorable name when you spawn it so you and other
  teammates can address it directly later.
- Watch for file conflicts: don't let two teammates own the same file at once.
- If a teammate stalls or starts doing work itself that should be delegated, redirect
  it rather than absorbing the work into your own context.
- Don't shut the team down while tasks remain open — check the shared task list, not
  just teammate self-reports.
