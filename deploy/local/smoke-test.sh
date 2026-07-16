#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"
env_file="${1:-}"
if [[ -z "$env_file" ]]; then
  env_file=deploy/local/.env.local.example
  [[ -f deploy/local/.env.local ]] && env_file=deploy/local/.env.local
fi
requested_port="${BACKEND_BIND_PORT:-}"

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a
if [[ -n "$requested_port" ]]; then
  export BACKEND_BIND_PORT="$requested_port"
fi
port="${BACKEND_BIND_PORT:-8080}"
compose=(docker compose --env-file "$env_file" --file deploy/local/compose.yml)

for attempt in $(seq 1 60); do
  status="$(curl --silent --output /dev/null --write-out '%{http_code}' "http://127.0.0.1:${port}/actuator/health" || true)"
  [[ "$status" == "200" ]] && break
  if [[ "$attempt" == "60" ]]; then
    "${compose[@]}" ps
    "${compose[@]}" logs --tail 100 backend
    exit 1
  fi
  sleep 2
done

health="$(curl --fail --silent --show-error "http://127.0.0.1:${port}/actuator/health")"
[[ "$health" == *'"status":"UP"'* ]]

unauth="$(curl --silent --show-error --write-out '|%{http_code}' "http://127.0.0.1:${port}/api/foods/search?keyword=%E9%B8%A1%E8%83%B8")"
[[ "$unauth" == *'"code":"AUTH_REQUIRED"'* ]]
[[ "$unauth" == *'|401' ]]

counts="$("${compose[@]}" exec -T --env MYSQL_PWD=local-diet-tracker-password mysql \
  mysql --user=diet_tracker --batch --skip-column-names diet_tracker \
  --execute="SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1; SELECT COUNT(*) FROM food_category; SELECT COUNT(*) FROM food_item WHERE user_id IS NULL;")"
flyway="$(printf '%s\n' "$counts" | sed -n '1p')"
categories="$(printf '%s\n' "$counts" | sed -n '2p')"
foods="$(printf '%s\n' "$counts" | sed -n '3p')"
[[ "$flyway" == "8" ]]
[[ "$categories" == "7" ]]
[[ "$foods" == "48" ]]

echo "Local smoke test passed: health=200 unauth=401 flyway=${flyway} categories=${categories} system_foods=${foods}"
