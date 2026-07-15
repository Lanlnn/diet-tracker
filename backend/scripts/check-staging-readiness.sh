#!/usr/bin/env bash
set -u

host="${STAGING_HOST:-staging.tigercloud.asia}"
base_url="https://${host}"
attempts="${STAGING_CHECK_ATTEMPTS:-3}"
failures=0

fail() {
  echo "FAIL: $1" >&2
  failures=$((failures + 1))
}

for command_name in dig curl; do
  command -v "$command_name" >/dev/null 2>&1 || fail "missing command: ${command_name}"
done

if ((failures > 0)); then
  exit 1
fi

for resolver in 1.1.1.1 8.8.8.8; do
  address="$(dig @"$resolver" +time=5 +tries=1 +short "$host" A | grep -E '^[0-9]+([.][0-9]+){3}$' | head -n 1)"
  [[ -n "$address" ]] || address="$(dig @"$resolver" +time=5 +tries=1 +short "$host" AAAA | grep -E '^[0-9a-fA-F:]+$' | head -n 1)"
  [[ -n "$address" ]] || fail "${host} has no public A/AAAA answer from ${resolver}"
done

check_status() {
  local label="$1"
  local expected="$2"
  local method="$3"
  local url="$4"
  local actual
  actual="$(curl --noproxy '*' --silent --show-error --output /dev/null --max-time 10 \
    --request "$method" --write-out '%{http_code}' "$url")" || actual="000"
  [[ "$actual" == "$expected" ]] || fail "${label} expected HTTP ${expected}, got ${actual}"
}

for ((attempt = 1; attempt <= attempts; attempt++)); do
  check_status "health attempt ${attempt}" 200 GET "${base_url}/actuator/health"
  check_status "unauthenticated food search attempt ${attempt}" 401 GET "${base_url}/api/foods/search?keyword=%E9%B8%A1%E8%83%B8"
  check_status "production seed isolation attempt ${attempt}" 404 POST "${base_url}/api/setup/seed"
done

if ((failures > 0)); then
  echo "Staging readiness check failed with ${failures} issue(s)" >&2
  exit 1
fi

echo "Staging DNS, TLS and HTTP readiness checks passed (${attempts} attempt(s))"
