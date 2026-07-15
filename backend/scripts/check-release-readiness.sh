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
test -f deploy/staging/compose.yml
test -f deploy/staging/nginx-staging.conf.example
test -x deploy/staging/preflight.sh
test -x deploy/staging/build-image.sh
test -x deploy/staging/smoke-test.sh
search '^FROM eclipse-temurin:17-jre-jammy$' backend/Dockerfile
search '^    image: mysql:8[.]0[.]46$' deploy/staging/compose.yml
search '^      - 127[.]0[.]0[.]1:\$\{BACKEND_BIND_PORT:-18080}:8080$' deploy/staging/compose.yml
search '^    read_only: true$' deploy/staging/compose.yml
bash -n backend/scripts/check-e1-environment.sh
bash -n backend/scripts/check-staging-readiness.sh
bash -n deploy/staging/preflight.sh
bash -n deploy/staging/build-image.sh
bash -n deploy/staging/smoke-test.sh

echo "Release readiness checks passed"
