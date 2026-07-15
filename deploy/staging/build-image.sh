#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
env_file="${1:-$repo_root/deploy/staging/.env}"
cd "$repo_root"

if [[ "${ALLOW_DIRTY_BUILD:-false}" != "true" ]] && [[ -n "$(git status --short)" ]]; then
  echo "Refusing to build a release image from a dirty worktree" >&2
  exit 1
fi

bash deploy/staging/preflight.sh "$env_file"

java_command="java"
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  java_command="$JAVA_HOME/bin/java"
fi
java_version="$("$java_command" -version 2>&1 | head -n 1)"
[[ "$java_version" =~ version[[:space:]]+\"17[.] ]] || {
  echo "Java 17 is required to build the staging artifact" >&2
  exit 1
}

(cd backend && sh mvnw --batch-mode clean package)
docker compose --env-file "$env_file" --file deploy/staging/compose.yml build --pull backend

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a
image_id="$(docker image inspect "diet-tracker-backend:${BACKEND_IMAGE_TAG}" --format '{{.Id}}')"
echo "Built diet-tracker-backend:${BACKEND_IMAGE_TAG} (${image_id})"
