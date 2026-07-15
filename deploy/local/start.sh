#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"

env_file="deploy/local/.env.local.example"
if [[ -f deploy/local/.env.local ]]; then
  env_file="deploy/local/.env.local"
fi

java_home="${JAVA_HOME:-}"
if [[ -z "$java_home" && -d /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ]]; then
  java_home=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
fi
if [[ -n "$java_home" ]]; then
  export JAVA_HOME="$java_home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

java_version="$(java -version 2>&1 | head -n 1 || true)"
[[ "$java_version" =~ version[[:space:]]+\"17[.] ]] || {
  echo "Java 17 is required; current runtime: ${java_version:-not found}" >&2
  exit 1
}

(cd backend && sh mvnw --batch-mode package)
GIT_SHA="$(git rev-parse HEAD)" BUILD_TIME="$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  docker compose --env-file "$env_file" --file deploy/local/compose.yml up --detach --build
bash deploy/local/smoke-test.sh "$env_file"

echo "Local backend is ready at http://127.0.0.1:8080 and the configured LAN address."
