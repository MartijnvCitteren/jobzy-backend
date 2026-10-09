#!/usr/bin/env bash
# Startup smoke test: starts the executable jar with the dev profile against a real SQL Server and requires
# /actuator/health (incl. the database check) to report UP. Catches what the H2-based tests cannot: DDL that SQL
# Server rejects, missing runtime configuration, and beans that only fail at startup.
# Usage: smoke-test.sh <sqlserver-container> <jar>
#   with MSSQL_SA_PASSWORD set and the container's SQL Server published on localhost:${SQLSERVER_PORT:-1433}.
set -euo pipefail

container=$1
jar=$2
db_port=${SQLSERVER_PORT:-1433}
app_port=8090
health_url="http://localhost:$app_port/api/v1/actuator/health"
log=$(mktemp)
sqlcmd=(docker exec "$container" /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "${MSSQL_SA_PASSWORD:?}" -C -b)

echo "Waiting for SQL Server..."
attempt=0
until "${sqlcmd[@]}" -Q "SELECT 1" >/dev/null 2>&1; do
  attempt=$((attempt + 1))
  if [ "$attempt" -ge 60 ]; then
    echo "::error::SQL Server did not accept connections within 120 seconds."
    docker logs "$container" 2>&1 | tail -50
    exit 1
  fi
  sleep 2
done
"${sqlcmd[@]}" -Q "IF DB_ID('jobzy_db') IS NULL CREATE DATABASE jobzy_db"

echo "Starting $jar with the dev profile..."
SPRING_PROFILES_ACTIVE=dev \
  SPRING_DATASOURCE_URL="jdbc:sqlserver://localhost:$db_port;databaseName=jobzy_db;encrypt=true;trustServerCertificate=true" \
  SPRING_DATASOURCE_USERNAME=sa \
  SPRING_DATASOURCE_PASSWORD="$MSSQL_SA_PASSWORD" \
  java -jar "$jar" >"$log" 2>&1 &
app=$!
trap 'kill "$app" 2>/dev/null || true' EXIT

attempt=0
until health=$(curl -sf "$health_url"); do
  attempt=$((attempt + 1))
  if ! kill -0 "$app" 2>/dev/null; then
    echo "::error::The application exited during startup."
    cat "$log"
    exit 1
  fi
  if [ "$attempt" -ge 90 ]; then
    echo "::error::$health_url did not report UP within 90 seconds: $(curl -s "$health_url" || true)"
    cat "$log"
    exit 1
  fi
  sleep 1
done

if ! grep -q '"status":"UP"' <<<"$health"; then
  echo "::error::Health is not UP: $health"
  cat "$log"
  exit 1
fi
echo "Started against SQL Server, health: $health"
