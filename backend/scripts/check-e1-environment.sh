#!/usr/bin/env bash
set -u

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"

failures=0

fail() {
  echo "FAIL: $1" >&2
  failures=$((failures + 1))
}

require_value() {
  local name="$1"
  local value="${!name:-}"
  if [[ -z "$value" || "$value" == replace-with-* ]]; then
    fail "${name} is not configured"
  fi
}

required=(
  DB_URL DB_USERNAME DB_PASSWORD WECHAT_APPID WECHAT_SECRET JWT_SECRET
  DELETION_AUDIT_PEPPER APP_BASE_URL UPLOAD_DIR CORS_ALLOWED_ORIGINS
  SPRING_PROFILES_ACTIVE RATE_LIMIT_ENABLED
)

for name in "${required[@]}"; do
  require_value "$name"
done

expected_appid="$(sed -n 's/.*"appid"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' miniapp/project.config.json | head -n 1)"
[[ -n "$expected_appid" ]] || fail "cannot read appid from miniapp/project.config.json"
[[ "${WECHAT_APPID:-}" == "$expected_appid" ]] || fail "WECHAT_APPID does not match miniapp/project.config.json"

[[ "${SPRING_PROFILES_ACTIVE:-}" == "prod" ]] || fail "SPRING_PROFILES_ACTIVE must be prod"
[[ "${RATE_LIMIT_ENABLED:-}" == "true" ]] || fail "RATE_LIMIT_ENABLED must be true"
[[ "${DB_URL:-}" == jdbc:mysql://* ]] || fail "DB_URL must use MySQL JDBC"
if [[ "${DB_URL:-}" == *"127.0.0.1"* || "${DB_URL:-}" == *"localhost"* ]]; then
  fail "DB_URL must point to the independent staging MySQL instance"
fi

[[ "${APP_BASE_URL:-}" == "https://staging.tigercloud.asia" ]] || fail "APP_BASE_URL must be https://staging.tigercloud.asia"
[[ "${CORS_ALLOWED_ORIGINS:-}" != *"*"* ]] || fail "CORS_ALLOWED_ORIGINS must not contain a wildcard"
jwt_secret="${JWT_SECRET:-}"
deletion_audit_pepper="${DELETION_AUDIT_PEPPER:-}"
[[ "${#jwt_secret}" -ge 32 ]] || fail "JWT_SECRET must contain at least 32 ASCII characters"
[[ "${#deletion_audit_pepper}" -ge 32 ]] || fail "DELETION_AUDIT_PEPPER must contain at least 32 ASCII characters"
[[ "${JWT_SECRET:-}" != "${DELETION_AUDIT_PEPPER:-}" ]] || fail "DELETION_AUDIT_PEPPER must be independent from JWT_SECRET"

if [[ -n "${UPLOAD_DIR:-}" ]]; then
  [[ "$UPLOAD_DIR" == /* ]] || fail "UPLOAD_DIR must be an absolute persistent path"
  [[ -d "$UPLOAD_DIR" ]] || fail "UPLOAD_DIR does not exist"
  [[ -w "$UPLOAD_DIR" ]] || fail "UPLOAD_DIR is not writable by the service account"
fi

if ((failures > 0)); then
  echo "E1 environment check failed with ${failures} issue(s); no secret values were printed" >&2
  exit 1
fi

echo "E1 environment check passed; no secret values were printed"
