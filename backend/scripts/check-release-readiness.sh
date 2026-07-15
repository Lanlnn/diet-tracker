#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"

required_placeholders=(DB_URL DB_USERNAME DB_PASSWORD WECHAT_APPID WECHAT_SECRET JWT_SECRET APP_BASE_URL CORS_ALLOWED_ORIGINS DELETION_AUDIT_PEPPER)
for name in "${required_placeholders[@]}"; do
  if ! rg -q "\\$\\{${name}" backend/src/main/resources; then
    echo "Missing environment-backed production setting: ${name}" >&2
    exit 1
  fi
done

if git grep -nE '(wechat\.secret|jwt\.secret|spring\.datasource\.password)=[^$]' -- ':!*.example'; then
  echo "Potential hard-coded secret found" >&2
  exit 1
fi

if ! rg -q '@Profile\("local"\)' backend/src/main/java/com/diettracker/controller/SetupController.java; then
  echo "Seed endpoint must stay local-only" >&2
  exit 1
fi

test -f backend/src/main/resources/db/rollback/U8__user_goals_and_deletion_audit.sql
rg -q "DELETE FROM flyway_schema_history WHERE version = '8'" backend/src/main/resources/db/rollback/U8__user_goals_and_deletion_audit.sql
rg -q '<java.version>17</java.version>' backend/pom.xml
test "$(rg -c 'java-version: 17' .github/workflows/ci.yml)" -eq 2
rg -q 'name: Backend tests \(MySQL 8\)' .github/workflows/ci.yml
rg -q 'TEST_DB_DRIVER: com.mysql.cj.jdbc.Driver' .github/workflows/ci.yml
rg -q '^app.rate-limit.enabled=true$' backend/src/main/resources/application-prod.properties
rg -q '^management.endpoint.health.probes.enabled=true$' backend/src/main/resources/application-prod.properties
rg -q '^management.endpoints.web.exposure.include=health,info,prometheus$' backend/src/main/resources/application-prod.properties
rg -q '^management.metrics.distribution.percentiles-histogram.http.server.requests=true$' backend/src/main/resources/application-prod.properties
test -f monitoring/alerts.yml
rg -q 'DietTrackerApiHighErrorRate' monitoring/alerts.yml
rg -q 'DietTrackerDashboardP95TooHigh' monitoring/alerts.yml
rg -q 'DietTrackerFoodSearchP95TooHigh' monitoring/alerts.yml
rg -q 'DietTrackerAnalyticsP95TooHigh' monitoring/alerts.yml

echo "Release readiness checks passed"
