#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"

search() {
  local pattern="$1"
  local path="$2"
  if command -v rg >/dev/null 2>&1; then
    rg -q "$pattern" "$path"
  elif [[ -d "$path" ]]; then
    grep -R -E -q "$pattern" "$path"
  else
    grep -E -q "$pattern" "$path"
  fi
}

required_placeholders=(DB_URL DB_USERNAME DB_PASSWORD WECHAT_APPID WECHAT_SECRET JWT_SECRET APP_BASE_URL CORS_ALLOWED_ORIGINS DELETION_AUDIT_PEPPER)
for name in "${required_placeholders[@]}"; do
  if ! search "\\$\\{${name}" backend/src/main/resources; then
    echo "Missing environment-backed production setting: ${name}" >&2
    exit 1
  fi
done

if git grep -nE '(wechat\.secret|jwt\.secret|spring\.datasource\.password)=[^$]' -- ':!*.example'; then
  echo "Potential hard-coded secret found" >&2
  exit 1
fi

if ! search '@Profile\("local"\)' backend/src/main/java/com/diettracker/controller/SetupController.java; then
  echo "Seed endpoint must stay local-only" >&2
  exit 1
fi

test -f backend/src/main/resources/db/rollback/U8__user_goals_and_deletion_audit.sql
search "DELETE FROM flyway_schema_history WHERE version = '8'" backend/src/main/resources/db/rollback/U8__user_goals_and_deletion_audit.sql
search '<java.version>17</java.version>' backend/pom.xml
test "$(grep -c 'java-version: 17' .github/workflows/ci.yml)" -eq 2
search 'name: Backend tests \(MySQL 8\)' .github/workflows/ci.yml
search 'TEST_DB_DRIVER: com.mysql.cj.jdbc.Driver' .github/workflows/ci.yml
search '^app.rate-limit.enabled=true$' backend/src/main/resources/application-prod.properties
search '^app.base-url=\$\{APP_BASE_URL}$' backend/src/main/resources/application-prod.properties
search '^app.upload.dir=\$\{UPLOAD_DIR}$' backend/src/main/resources/application-prod.properties
search '^app.cors.allowed-origins=\$\{CORS_ALLOWED_ORIGINS}$' backend/src/main/resources/application-prod.properties
search '^app.deletion.audit-pepper=\$\{DELETION_AUDIT_PEPPER}$' backend/src/main/resources/application-prod.properties
search '^management.endpoint.health.probes.enabled=true$' backend/src/main/resources/application-prod.properties
search '^management.endpoints.web.exposure.include=health,info,prometheus$' backend/src/main/resources/application-prod.properties
search '^management.metrics.distribution.percentiles-histogram.http.server.requests=true$' backend/src/main/resources/application-prod.properties
test -f monitoring/alerts.yml
search 'DietTrackerApiHighErrorRate' monitoring/alerts.yml
search 'DietTrackerDashboardP95TooHigh' monitoring/alerts.yml
search 'DietTrackerFoodSearchP95TooHigh' monitoring/alerts.yml
search 'DietTrackerAnalyticsP95TooHigh' monitoring/alerts.yml
test -f backend/Dockerfile
test -f deploy/local/compose.yml
test -f deploy/local/.env.local.example
test -x deploy/local/start.sh
test -x deploy/local/stop.sh
test -x deploy/local/reset-data.sh
test -x deploy/local/smoke-test.sh
test -x deploy/local/main-flow-test.mjs
test -x deploy/local/preview-fixture.mjs
search '^FROM eclipse-temurin:17-jre-jammy$' backend/Dockerfile
search '^    image: mysql:8[.]0[.]46$' deploy/local/compose.yml
search '^      - 0[.]0[.]0[.]0:\$\{BACKEND_BIND_PORT:-8080}:8080$' deploy/local/compose.yml
search '^    read_only: true$' deploy/local/compose.yml
bash -n backend/scripts/check-e1-environment.sh
bash -n deploy/local/start.sh
bash -n deploy/local/stop.sh
bash -n deploy/local/reset-data.sh
bash -n deploy/local/smoke-test.sh
node --check deploy/local/main-flow-test.mjs
node --check deploy/local/preview-fixture.mjs

echo "Release readiness checks passed"
