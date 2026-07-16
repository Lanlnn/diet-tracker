#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repo_root"

env_file="deploy/local/.env.local"
replace=false

usage() {
  cat <<'EOF'
Usage: bash deploy/local/init-wechat-env.sh [--replace]

Creates the Git-ignored local WeChat configuration with mode 600. The WeChat
Secret is read from the terminal without echo and is never printed.
EOF
}

case "${1:-}" in
  "") ;;
  --replace) replace=true ;;
  --help|-h) usage; exit 0 ;;
  *) usage >&2; exit 2 ;;
esac

if [[ -f "$env_file" && "$replace" != "true" ]]; then
  echo "$env_file already exists; use --replace only when you intend to rotate it" >&2
  exit 1
fi

command -v openssl >/dev/null 2>&1 || {
  echo "openssl is required to generate local-only signing secrets" >&2
  exit 1
}

if [[ ! -r /dev/tty ]]; then
  echo "Run this script in an interactive terminal so the WeChat Secret stays hidden" >&2
  exit 1
fi

expected_appid="$(sed -n 's/.*"appid"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' miniapp/project.config.json | head -n 1)"
[[ "$expected_appid" == "wx8cfee49a3f7392b2" ]] || {
  echo "The miniapp AppID does not match the frozen E1 AppID" >&2
  exit 1
}

printf 'WeChat Secret for %s (input hidden): ' "$expected_appid" >/dev/tty
IFS= read -r -s wechat_secret </dev/tty
printf '\nRepeat WeChat Secret: ' >/dev/tty
IFS= read -r -s wechat_secret_repeat </dev/tty
printf '\n' >/dev/tty

[[ "$wechat_secret" == "$wechat_secret_repeat" ]] || {
  echo "The two Secret values do not match" >&2
  exit 1
}
[[ "$wechat_secret" =~ ^[A-Za-z0-9_-]{16,128}$ ]] || {
  echo "The Secret format is unexpected; copy the AppSecret from the WeChat admin console" >&2
  exit 1
}

lan_ip="${LOCAL_LAN_IP:-}"
if [[ -z "$lan_ip" && "$(uname -s)" == "Darwin" ]]; then
  lan_ip="$(ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null || true)"
fi
if [[ -z "$lan_ip" ]] && command -v hostname >/dev/null 2>&1; then
  lan_ip="$(hostname -I 2>/dev/null | awk '{print $1}' || true)"
fi
lan_ip="${lan_ip:-192.168.3.25}"
[[ "$lan_ip" =~ ^([0-9]{1,3}[.]){3}[0-9]{1,3}$ ]] || {
  echo "Could not determine a valid LAN IPv4 address; rerun with LOCAL_LAN_IP=x.x.x.x" >&2
  exit 1
}

jwt_secret="$(openssl rand -hex 32)"
audit_pepper="$(openssl rand -hex 32)"
tmp_file="$(mktemp "${TMPDIR:-/tmp}/diet-tracker-env.XXXXXX")"
trap 'rm -f "$tmp_file"' EXIT
umask 077
{
  printf '# Generated locally. Never commit or share this file.\n'
  printf 'WECHAT_APPID=%s\n' "$expected_appid"
  printf 'WECHAT_SECRET=%s\n' "$wechat_secret"
  printf 'JWT_SECRET=%s\n' "$jwt_secret"
  printf 'DELETION_AUDIT_PEPPER=%s\n' "$audit_pepper"
  printf 'BACKEND_BIND_PORT=8080\n'
  printf 'LOCAL_LAN_IP=%s\n' "$lan_ip"
} >"$tmp_file"
chmod 600 "$tmp_file"
mv "$tmp_file" "$env_file"
trap - EXIT
unset wechat_secret wechat_secret_repeat jwt_secret audit_pepper

bash backend/scripts/check-e1-environment.sh --require-wechat
echo "Local WeChat configuration created for LAN ${lan_ip}. Next: bash deploy/local/start.sh"
