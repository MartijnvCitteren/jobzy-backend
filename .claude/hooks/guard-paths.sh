#!/usr/bin/env bash
# PreToolUse hook (Edit/Write/MultiEdit/NotebookEdit): blocks writes to build output and generated code,
# and asks the human before an agent edits guardrail files. See docs/adr/0001-agentic-development-guardrails.md.
set -euo pipefail

path=$(jq -r '.tool_input.file_path // .tool_input.notebook_path // ""')
[ -z "$path" ] && exit 0

proj="${CLAUDE_PROJECT_DIR:-$PWD}"
case "$path" in
  "$proj"/*) rel="${path#"$proj"/}" ;;
  /*) exit 0 ;; # outside the project: normal permission rules apply
  *) rel="$path" ;;
esac

deny() {
  echo "Blocked by .claude/hooks/guard-paths.sh: $1" >&2
  exit 2
}

ask() {
  jq -n --arg r "guard-paths.sh: $1" \
    '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "ask", permissionDecisionReason: $r}}'
  exit 0
}

case "/$rel" in
  */../*) ask "path contains '..', cannot verify it stays outside guardrail files: $rel" ;;
  */target/*) deny "$rel is build output. Change the source (e.g. the OpenAPI YAML) and rebuild instead." ;;
  */generated-sources/*) deny "$rel is generated code. Never hand-edit it; change the OpenAPI YAML instead." ;;
  /.claude/* | /.github/* | /.githooks/* | /.agents/* | /.mvn/* | /skills-lock.json | /mvnw | /mvnw.cmd | */ArchitectureTest.java)
    ask "$rel is a guardrail or agent-instruction file. Changes to the safety net need explicit human approval." ;;
esac

exit 0
