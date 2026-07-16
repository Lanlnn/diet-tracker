#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"
require_wechat=false
if [[ "${1:-}" == "--require-wechat" ]]; then
  require_wechat=true
elif [[ -n "${1:-}" ]]; then
  echo "Usage: $0 [--require-wechat]" >&2
  exit 2
fi

failures=0
fail() {
  echo "FAIL: $1" >&2
  failures=$((failures + 1))
}

command -v docker >/dev/null 2>&1 || fail "Docker is not installed"
docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 is unavailable"

java_command=java
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  java_command="$JAVA_HOME/bin/java"
elif [[ -x /opt/homebrew/opt/openjdk@17/bin/java ]]; then
  java_command=/opt/homebrew/opt/openjdk@17/bin/java
fi
java_version="$($java_command -version 2>&1 | head -n 1 || true)"
[[ "$java_version" =~ version[[:space:]]+\"17[.] ]] || fail "Java 17 is required"

expected_appid="$(sed -n 's/.*"appid"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' miniapp/project.config.json | head -n 1)"
[[ "$expected_appid" == "wx8cfee49a3f7392b2" ]] || fail "miniapp AppID differs from the frozen E1 AppID"

grep -q "develop: 'http://192[.]168[.]3[.]25:8080/api'" miniapp/shared/config.js || fail "develop API is not the frozen LAN address"
grep -q "trial: 'http://192[.]168[.]3[.]25:8080/api'" miniapp/shared/config.js || fail "trial API is not the frozen LAN address"
grep -q '^    image: mysql:8[.]0[.]46$' deploy/local/compose.yml || fail "local MySQL must be pinned to 8.0.46"
grep -q '^      - 0[.]0[.]0[.]0:' deploy/local/compose.yml || fail "backend must listen on the LAN interface"

docker compose --env-file deploy/local/.env.local.example --file deploy/local/compose.yml config --quiet \
  || fail "local compose configuration is invalid"

if [[ -f deploy/local/.env.local ]]; then
  if grep -q 'local-placeholder-replace-with-real-secret' deploy/local/.env.local; then
    fail "deploy/local/.env.local still contains the placeholder WECHAT_SECRET"
  fi
  if git ls-files --error-unmatch deploy/local/.env.local >/dev/null 2>&1; then
    fail "deploy/local/.env.local must never be tracked"
  fi
elif [[ "$require_wechat" == "true" ]]; then
  fail "deploy/local/.env.local is required for real WeChat login"
fi

if [[ "$require_wechat" == "true" && -f deploy/local/.env.local ]]; then
  if [[ "$(uname -s)" == "Darwin" ]]; then
    env_mode="$(stat -f '%Lp' deploy/local/.env.local)"
  else
    env_mode="$(stat -c '%a' deploy/local/.env.local)"
  fi
  [[ "$env_mode" == "600" ]] || fail "deploy/local/.env.local must have mode 600"

  configured_appid="$(sed -n 's/^WECHAT_APPID=//p' deploy/local/.env.local | tail -n 1)"
  configured_secret="$(sed -n 's/^WECHAT_SECRET=//p' deploy/local/.env.local | tail -n 1)"
  [[ "$configured_appid" == "$expected_appid" ]] || fail "local WECHAT_APPID does not match miniapp/project.config.json"
  [[ -n "$configured_secret" ]] || fail "local WECHAT_SECRET is empty"
  [[ "$configured_secret" != *placeholder* ]] || fail "local WECHAT_SECRET is still a placeholder"
fi

if ((failures > 0)); then
  echo "E1 local environment check failed with ${failures} issue(s)" >&2
  exit 1
fi

if [[ "$require_wechat" == "true" ]]; then
  echo "E1 WeChat login configuration is ready for a real wx.login test; no secret value was printed"
else
  echo "E1 local environment check passed (Java 17, MySQL 8.0.46, AppID, LAN API and Compose)"
fi
