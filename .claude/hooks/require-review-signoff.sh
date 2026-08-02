#!/bin/bash
# TaskCompleted gate: a task can't be marked complete without a review sign-off marker
# in its description. Verified against a real TaskCompleted payload (fields are
# task_id / task_subject / task_description, no task_metadata) rather than guessed.
#
# TaskCompleted has no matcher support, so this fires for every task in the team,
# not only backend implementation tasks. Non-implementation tasks (architecture plan,
# docs, retro) need the marker too -- the lead or reviewer can append a short
# "Reviewed-by: jobzy-code-reviewer (n/a - <reason>)" note for tasks where a full
# three-axis review doesn't apply.

INPUT=$(cat)
SUBJECT=$(echo "$INPUT" | jq -r '.task_subject // "(unknown task)"')
DESCRIPTION=$(echo "$INPUT" | jq -r '.task_description // ""')

if ! echo "$DESCRIPTION" | grep -qi "Reviewed-by: jobzy-code-reviewer"; then
  echo "Task '$SUBJECT' cannot be marked complete yet: its description is missing a 'Reviewed-by: jobzy-code-reviewer' sign-off marker. Get the jobzy-code-reviewer teammate to review this task's changes on all three axes (coding standards, architecture, functionality), then append the marker to the task description before retrying." >&2
  exit 2
fi

exit 0
