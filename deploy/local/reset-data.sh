#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"
env_file="deploy/local/.env.local.example"
[[ -f deploy/local/.env.local ]] && env_file="deploy/local/.env.local"

if [[ "${1:-}" != "--confirm" ]]; then
  echo "This deletes only the local MySQL and avatar Docker volumes." >&2
  echo "Run: bash deploy/local/reset-data.sh --confirm" >&2
  exit 1
fi

docker compose --env-file "$env_file" --file deploy/local/compose.yml down --volumes --remove-orphans
echo "Local data was removed. Run deploy/local/start.sh to recreate it."
