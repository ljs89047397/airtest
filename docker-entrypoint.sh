#!/bin/sh
set -e

# Railway PostgreSQL env vars: PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE
# Custom vars: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS
DB_HOST="${DB_HOST:-${PGHOST:-localhost}}"
DB_PORT="${DB_PORT:-${PGPORT:-5432}}"
DB_NAME="${DB_NAME:-${PGDATABASE:-icas}}"
DB_USER="${DB_USER:-${PGUSER:-icas_admin}}"
DB_PASS="${DB_PASS:-${PGPASSWORD:-icas1234!}}"

JDBC_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?currentSchema=com,emp,er,vr,saf,ptl"

export CATALINA_OPTS="${CATALINA_OPTS} \
  -Ddb.url=${JDBC_URL} \
  -Ddb.username=${DB_USER} \
  -Ddb.password=${DB_PASS}"

exec catalina.sh run
