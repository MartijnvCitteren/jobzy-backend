#!/bin/bash
# TaskCompleted gate: the team's single "[review-gate]" task per feature can't be marked
# complete without a review sign-off marker in its description. Verified against a real
# TaskCompleted payload (fields are task_id / task_subject / task_description, no
# task_metadata) rather than guessed.
#
# TaskCompleted has no matcher support, so this fires for every task in the team. By
# design this team reviews once per feature, not per task (see jobzy-architect and
# jobzy-code-reviewer) -- so this hook only enforces the marker on the task whose
# subject is tagged "[review-gate]" (created by jobzy-architect as the last task in
# every plan, depending on all implementation tasks). Every other task passes through
# untouched, regardless of its description.

INPUT=$(cat)
SUBJECT=$(echo "$INPUT" | jq -r '.task_subject // "(unknown task)"')
DESCRIPTION=$(echo "$INPUT" | jq -r '.task_description // ""')

if ! echo "$SUBJECT" | grep -qi '\[review-gate\]'; then
  exit 0
fi

if ! echo "$DESCRIPTION" | grep -qi "Reviewed-by: jobzy-code-reviewer"; then
  echo "Task '$SUBJECT' cannot be marked complete yet: its description is missing a 'Reviewed-by: jobzy-code-reviewer' sign-off marker. Get the jobzy-code-reviewer teammate to review the full feature diff on all three axes (coding standards, architecture, functionality), then append the marker to this task's description before retrying." >&2
  exit 2
fi

exit 0
