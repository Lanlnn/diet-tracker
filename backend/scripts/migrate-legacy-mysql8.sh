#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
SQL_FILE="${BACKEND_DIR}/src/main/resources/db/legacy/upgrade_legacy_schema_to_v5.sql"
BACKUP_DIR="${BACKEND_DIR}/var/backups"

HOST="${MYSQL_MIGRATION_HOST:-127.0.0.1}"
PORT="${MYSQL_MIGRATION_PORT:-3306}"
DATABASE="${MYSQL_MIGRATION_DATABASE:-}"
USERNAME="${MYSQL_MIGRATION_USERNAME:-}"
PASSWORD="${MYSQL_MIGRATION_PASSWORD-}"
CONFIRM_DATABASE="${MIGRATION_CONFIRM_DATABASE:-}"
LEGACY_USER_ID="${LEGACY_USER_ID:-}"

fail() {
    printf 'Migration aborted: %s\n' "$1" >&2
    exit 1
}

[[ -n "${DATABASE}" ]] || fail "MYSQL_MIGRATION_DATABASE is required"
[[ -n "${USERNAME}" ]] || fail "MYSQL_MIGRATION_USERNAME is required"
[[ "${CONFIRM_DATABASE}" == "${DATABASE}" ]] || fail "MIGRATION_CONFIRM_DATABASE must exactly match MYSQL_MIGRATION_DATABASE"
[[ "${DATABASE}" =~ ^[A-Za-z0-9_]+$ ]] || fail "database name may contain only letters, digits, and underscores"
[[ "${PORT}" =~ ^[0-9]+$ ]] || fail "MYSQL_MIGRATION_PORT must be numeric"
if [[ -n "${LEGACY_USER_ID}" && ! "${LEGACY_USER_ID}" =~ ^[A-Za-z0-9_:@.-]{1,100}$ ]]; then
    fail "LEGACY_USER_ID contains unsupported characters or exceeds 100 characters"
fi

for command in mysql mysqldump; do
    command -v "${command}" >/dev/null 2>&1 || fail "${command} is required"
done
[[ -f "${SQL_FILE}" ]] || fail "legacy upgrade SQL is missing"

export MYSQL_PWD="${PASSWORD}"
MYSQL=(mysql --protocol=TCP --host="${HOST}" --port="${PORT}" --user="${USERNAME}" --batch --skip-column-names)

query() {
    "${MYSQL[@]}" "$@"
}

server_version="$(query --execute='SELECT VERSION()')"
major_version="${server_version%%.*}"
[[ "${major_version}" =~ ^[0-9]+$ && "${major_version}" -ge 8 ]] || fail "target server must be MySQL 8 or newer"

database_exists="$(query --execute="SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name='${DATABASE}'")"
[[ "${database_exists}" == "1" ]] || fail "target database does not exist"

for table in food_category food_item meal_record; do
    exists="$(query --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DATABASE}' AND table_name='${table}'")"
    [[ "${exists}" == "1" ]] || fail "required legacy table ${table} is missing"
done

flyway_exists="$(query --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DATABASE}' AND table_name='flyway_schema_history'")"
if [[ "${flyway_exists}" == "1" ]]; then
    fail "flyway_schema_history already exists; use the normal application Flyway migration path"
fi

meal_user_column="$(query --execute="SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='${DATABASE}' AND table_name='meal_record' AND column_name='user_id'")"
if [[ "${meal_user_column}" == "1" ]]; then
    unowned_meals="$(query "${DATABASE}" --execute='SELECT COUNT(*) FROM meal_record WHERE user_id IS NULL')"
else
    unowned_meals="$(query "${DATABASE}" --execute='SELECT COUNT(*) FROM meal_record')"
fi
if [[ "${unowned_meals}" -gt 0 && -z "${LEGACY_USER_ID}" ]]; then
    fail "${unowned_meals} meal records have no owner; set LEGACY_USER_ID to the OpenID that should own them"
fi

count_rows() {
    local table="$1"
    local exists
    exists="$(query --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DATABASE}' AND table_name='${table}'")"
    if [[ "${exists}" == "1" ]]; then
        query "${DATABASE}" --execute="SELECT COUNT(*) FROM ${table}"
    else
        printf '0\n'
    fi
}

tables=(users food_category food_item meal_record food_favorite)
before_users="$(count_rows users)"
before_food_category="$(count_rows food_category)"
before_food_item="$(count_rows food_item)"
before_meal_record="$(count_rows meal_record)"
before_food_favorite="$(count_rows food_favorite)"

before_count() {
    case "$1" in
        users) printf '%s\n' "${before_users}" ;;
        food_category) printf '%s\n' "${before_food_category}" ;;
        food_item) printf '%s\n' "${before_food_item}" ;;
        meal_record) printf '%s\n' "${before_meal_record}" ;;
        food_favorite) printf '%s\n' "${before_food_favorite}" ;;
        *) fail "unexpected table in row-count validation" ;;
    esac
}

mkdir -p "${BACKUP_DIR}"
timestamp="$(date +%Y%m%d-%H%M%S)"
backup_file="${BACKUP_DIR}/${DATABASE}-before-flyway-v5-${timestamp}.sql"
printf 'Creating backup: %s\n' "${backup_file}"
mysqldump \
    --protocol=TCP --host="${HOST}" --port="${PORT}" --user="${USERNAME}" \
    --single-transaction --routines --triggers --no-tablespaces --set-gtid-purged=OFF \
    --databases "${DATABASE}" > "${backup_file}"
[[ -s "${backup_file}" ]] || fail "backup file is empty"

legacy_user_sql="${LEGACY_USER_ID//\'/\'\'}"
printf 'Normalizing legacy schema and preserving existing rows...\n'
query --init-command="SET @legacy_user_id='${legacy_user_sql}'" "${DATABASE}" < "${SQL_FILE}"

jdbc_url="jdbc:mysql://${HOST}:${PORT}/${DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia%2FShanghai"
printf 'Registering the normalized schema as Flyway V5...\n'
(
    cd "${BACKEND_DIR}"
    DB_URL="${jdbc_url}" \
    DB_USERNAME="${USERNAME}" \
    DB_PASSWORD="${PASSWORD}" \
    MIGRATION_CONFIRM_DATABASE="${DATABASE}" \
        sh mvnw --batch-mode -DskipTests compile exec:java \
        -Dexec.mainClass=com.diettracker.tools.LegacyMysqlFlywayAdopter \
        -Dexec.cleanupDaemonThreads=false
)

printf 'Running Flyway and Hibernate schema validation...\n'
(
    cd "${BACKEND_DIR}"
    MYSQL_INTEGRATION_TEST=true \
    MYSQL_TEST_DB_URL="${jdbc_url}" \
    MYSQL_TEST_DB_USERNAME="${USERNAME}" \
    MYSQL_TEST_DB_PASSWORD="${PASSWORD}" \
        sh mvnw --batch-mode -Dtest=MySqlMigrationTest test
)

for table in "${tables[@]}"; do
    after_count="$(count_rows "${table}")"
    table_before="$(before_count "${table}")"
    if [[ "${after_count}" -lt "${table_before}" ]]; then
        fail "row count decreased for ${table}; restore from ${backup_file}"
    fi
    printf '%-16s before=%s after=%s\n' "${table}" "${table_before}" "${after_count}"
done

unset MYSQL_PWD
printf 'Legacy MySQL migration completed successfully.\n'
printf 'Backup retained at: %s\n' "${backup_file}"
