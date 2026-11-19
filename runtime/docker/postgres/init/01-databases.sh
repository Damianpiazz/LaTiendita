#!/bin/sh
# One database per microservice inside a single lightweight PostgreSQL cluster.
# The official entrypoint runs this only on a fresh pgdata volume.
set -eu

for db in product orders payment shipment customer cart offer review; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    -c "CREATE DATABASE $db OWNER \"$POSTGRES_USER\";"
done
