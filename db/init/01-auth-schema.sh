#!/bin/bash


set -euo pipefail

psql -v ON_ERROR_STOP=1 \
     --username "$POSTGRES_USER" \
     --dbname "$POSTGRES_DB" \
     -v auth_password="$AUTH_SVC_PASSWORD" <<'EOSQL'
CREATE ROLE auth_svc LOGIN PASSWORD :'auth_password';

CREATE SCHEMA auth AUTHORIZATION auth_svc;

-- По умолчанию auth_svc работает в своей схеме
ALTER ROLE auth_svc SET search_path = auth;
EOSQL
