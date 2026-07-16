#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"
env_file="deploy/local/.env.local.example"
[[ -f deploy/local/.env.local ]] && env_file="deploy/local/.env.local"

docker compose --env-file "$env_file" --file deploy/local/compose.yml down
