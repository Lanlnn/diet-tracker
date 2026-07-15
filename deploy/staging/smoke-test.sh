#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"

project="diet-tracker-e1-smoke-$$"
bind_port="${SMOKE_BIND_PORT:-18081}"
upload_dir="/tmp/${project}-uploads"
git_sha="$(git rev-parse HEAD)"
image_tag="${SMOKE_IMAGE_TAG:-e1-local}"

mkdir -p "$upload_dir"
docker run --rm --user 0 --volume "$upload_dir:/uploads" eclipse-temurin:17-jre-jammy \
  chown 10001:10001 /uploads

common_env=(
  BACKEND_IMAGE_TAG="$image_tag"
  BACKEND_BIND_PORT="$bind_port"
  GIT_SHA="$git_sha"
  BUILD_TIME=2026-07-16T00:00:00Z
  'DB_URL=jdbc:mysql://mysql:3306/diet_tracker_staging?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia%2FShanghai&useSSL=false&allowPublicKeyRetrieval=true'
  DB_USERNAME=diet_tracker_staging
  DB_PASSWORD=test-database-password
  MYSQL_ROOT_PASSWORD=test-root-password
  WECHAT_APPID=wx8cfee49a3f7392b2
  WECHAT_SECRET=test-wechat-secret
  JWT_SECRET=test-jwt-secret-at-least-32-characters-long
  DELETION_AUDIT_PEPPER=test-independent-pepper-at-least-32-characters
  APP_BASE_URL=https://staging.tigercloud.asia
  UPLOAD_DIR="$upload_dir"
  CORS_ALLOWED_ORIGINS=https://staging.tigercloud.asia
  SPRING_PROFILES_ACTIVE=prod
  RATE_LIMIT_ENABLED=true
  RATE_LIMIT_REQUESTS_PER_MINUTE=120
)

compose() {
  env "${common_env[@]}" docker compose -p "$project" -f deploy/staging/compose.yml "$@"
}

cleanup() {
  compose down --volumes --remove-orphans >/dev/null 2>&1 || true
  docker run --rm --user 0 --volume "$upload_dir:/uploads" eclipse-temurin:17-jre-jammy \
    chown "$(id -u):$(id -g)" /uploads >/dev/null 2>&1 || true
  rm -rf "$upload_dir" || true
}
trap cleanup EXIT

compose up --detach --no-build

for attempt in $(seq 1 50); do
  http_status="$(curl --silent --output /dev/null --write-out '%{http_code}' \
    "http://127.0.0.1:${bind_port}/actuator/health" || true)"
  [[ "$http_status" == "200" ]] && break
  if [[ "$attempt" == "50" ]]; then
    compose ps
    compose logs --tail 100 backend
    exit 1
  fi
  sleep 2
done

health_body="$(curl --fail --silent --show-error "http://127.0.0.1:${bind_port}/actuator/health")"
[[ "$health_body" == *'"status":"UP"'* ]]

unauth_body="$(curl --silent --show-error --write-out '|%{http_code}' \
  "http://127.0.0.1:${bind_port}/api/foods/search?keyword=%E9%B8%A1%E8%83%B8")"
[[ "$unauth_body" == *'"code":"AUTH_REQUIRED"'* ]]
[[ "$unauth_body" == *'"requestId":'* ]]
[[ "$unauth_body" == *'|401' ]]

seed_status="$(curl --silent --show-error --output /dev/null --request POST --write-out '%{http_code}' \
  "http://127.0.0.1:${bind_port}/api/setup/seed")"
[[ "$seed_status" == "404" ]]

database_counts="$(compose exec -T --env MYSQL_PWD=test-database-password mysql \
  mysql --user=diet_tracker_staging --batch --skip-column-names diet_tracker_staging \
  --execute="SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success=1; SELECT COUNT(*) FROM food_category; SELECT COUNT(*) FROM food_item WHERE user_id IS NULL;")"
flyway_version="$(printf '%s\n' "$database_counts" | sed -n '1p')"
category_count="$(printf '%s\n' "$database_counts" | sed -n '2p')"
system_food_count="$(printf '%s\n' "$database_counts" | sed -n '3p')"
[[ "$flyway_version" == "8" ]]
[[ "$category_count" -gt 0 ]]
[[ "$system_food_count" -gt 0 ]]

backend_id="$(compose ps -q backend)"
mysql_id="$(compose ps -q mysql)"
docker exec "$backend_id" sh -c "touch '$upload_dir/.write-probe' && rm '$upload_dir/.write-probe'"
[[ "$(docker inspect "$backend_id" --format '{{.Config.User}}')" == "10001:10001" ]]
[[ "$(docker inspect "$backend_id" --format '{{.HostConfig.ReadonlyRootfs}}')" == "true" ]]
[[ -z "$(docker port "$mysql_id")" ]]
[[ "$(docker image inspect "diet-tracker-backend:${image_tag}" --format '{{index .Config.Labels "org.opencontainers.image.revision"}}')" == "$git_sha" ]]

echo "Staging smoke test passed: health=200 unauth=401 seed=404 flyway=${flyway_version} categories=${category_count} system_foods=${system_food_count}"
