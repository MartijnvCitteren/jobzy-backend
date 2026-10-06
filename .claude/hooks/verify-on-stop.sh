#!/usr/bin/env bash
# Stop hook: the agent may only finish when `./mvnw verify` is green for the current working tree.
# Formats the code first (spotless:apply), so formatting never blocks on its own.
#
# - Skips the build when no build-relevant file changed since the last green run (plain Q&A turns stay fast).
# - After 3 consecutive red runs within one turn the agent may stop, and the human is warned that the build is red.
# - Disable for a whole session by starting Claude Code with CLAUDE_SKIP_VERIFY_ON_STOP=1.
#
# State (last green fingerprint, lock, log) lives in .git/claude-guard/, so it survives `mvn clean` and is never committed.
set -uo pipefail

input=$(cat)
[ "${CLAUDE_SKIP_VERIFY_ON_STOP:-}" = "1" ] && exit 0

proj="${CLAUDE_PROJECT_DIR:-$PWD}"
cd "$proj" || exit 0
git_dir=$(git rev-parse --absolute-git-dir 2>/dev/null) || exit 0
state="$git_dir/claude-guard"
mkdir -p "$state"
log="$state/last-verify.log"
max_blocks=3

pathspec=(':(glob)**/*.java' ':(glob)**/*.xml' ':(glob)**/*.yml' ':(glob)**/*.yaml'
  ':(glob)**/*.properties' ':(glob)**/*.sql' '.mvn' 'mvnw')

fingerprint() {
  {
    git rev-parse HEAD
    git diff HEAD --binary -- "${pathspec[@]}"
    git ls-files --others --exclude-standard -- "${pathspec[@]}" | while IFS= read -r f; do shasum "$f"; done
  } | shasum | cut -d' ' -f1
}

dirty() { [ -n "$(git status --porcelain -- "${pathspec[@]}")" ]; }

fp=$(fingerprint)
last_green=$(cat "$state/last-green" 2>/dev/null || true)

# First run in this clone with a clean tree: trust the committed state (CI verifies it) as the baseline.
if [ -z "$last_green" ] && ! dirty; then
  echo "$fp" >"$state/last-green"
  exit 0
fi
if [ "$fp" = "$last_green" ]; then
  rm -f "$state/blocks"
  exit 0
fi

# Serialise builds when several sessions stop at the same time (they share target/).
lock="$state/lock"
for _ in $(seq 1 300); do
  mkdir "$lock" 2>/dev/null && break
  if [ -n "$(find "$lock" -maxdepth 0 -mmin +15 2>/dev/null)" ]; then rmdir "$lock" 2>/dev/null; fi
  sleep 1
done
trap 'rmdir "$lock" 2>/dev/null' EXIT

./mvnw -B -q spotless:apply >"$log" 2>&1
./mvnw -B verify >>"$log" 2>&1
status=$?

if [ "$status" -eq 0 ]; then
  fingerprint >"$state/last-green"
  rm -f "$state/blocks"
  exit 0
fi

# A fresh stop (not a continuation forced by this hook) starts a new attempt count.
if [ "$(jq -r '.stop_hook_active // false' <<<"$input")" != "true" ]; then rm -f "$state/blocks"; fi
blocks=$(($(cat "$state/blocks" 2>/dev/null || echo 0) + 1))
echo "$blocks" >"$state/blocks"

if [ "$blocks" -gt "$max_blocks" ]; then
  rm -f "$state/blocks"
  jq -n --arg m "WARNING: ./mvnw verify is still RED after $max_blocks attempts; the agent was allowed to stop. Log: $log" \
    '{systemMessage: $m}'
  exit 0
fi

{
  echo "./mvnw verify is RED (attempt $blocks of $max_blocks). You are not done: fix the cause, do not weaken tests or checks."
  echo "If the failure is not caused by your change or a check is wrong, say so explicitly to the human instead."
  echo "Full log: $log"
  echo "---"
  grep -E '^\[ERROR\]|Tests run:.*(Failures: [1-9]|Errors: [1-9])' "$log" |
    grep -vE '^\[ERROR\] *$|-> \[Help|Re-run Maven|full stack trace|For more information|^\[ERROR\] *\[Help' |
    head -40
} >&2
exit 2
