#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
env_file="${1:-$repo_root/deploy/staging/.env}"

if [[ ! -f "$env_file" ]]; then
  echo "Missing staging environment file: ${env_file}" >&2
  exit 1
fi

if [[ "$(uname -s)" == "Darwin" ]]; then
  env_mode="$(stat -f '%Lp' "$env_file")"
else
  env_mode="$(stat -c '%a' "$env_file")"
fi
if [[ "$env_mode" != "600" ]]; then
  echo "Staging environment file must have mode 600" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

cd "$repo_root"

expected_sha="$(git rev-parse HEAD)"
[[ "$GIT_SHA" == "$expected_sha" ]] || {
  echo "GIT_SHA does not match the checked-out commit" >&2
  exit 1
}
[[ "$BACKEND_IMAGE_TAG" == "$GIT_SHA" ]] || {
  echo "BACKEND_IMAGE_TAG must equal GIT_SHA" >&2
  exit 1
}
[[ "$BUILD_TIME" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$ ]] || {
  echo "BUILD_TIME must use UTC ISO-8601, for example 2026-07-16T00:00:00Z" >&2
  exit 1
}

if [[ "$(uname -s)" == "Darwin" ]]; then
  upload_uid="$(stat -f '%u' "$UPLOAD_DIR")"
  upload_gid="$(stat -f '%g' "$UPLOAD_DIR")"
else
  upload_uid="$(stat -c '%u' "$UPLOAD_DIR")"
  upload_gid="$(stat -c '%g' "$UPLOAD_DIR")"
fi
[[ "$upload_uid" == "10001" && "$upload_gid" == "10001" ]] || {
  echo "UPLOAD_DIR must be owned by UID/GID 10001:10001" >&2
  exit 1
}

bash backend/scripts/check-release-readiness.sh
bash backend/scripts/check-e1-environment.sh
docker compose --env-file "$env_file" --file deploy/staging/compose.yml config --quiet

echo "Staging deployment preflight passed for ${GIT_SHA}"
