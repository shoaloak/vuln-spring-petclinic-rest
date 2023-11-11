#!/bin/bash

# Get absolute script path
readonly SCRIPT_PATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

docker run \
  --rm \
  --detach \
  --name petclinic-db-postgresql \
  -e POSTGRES_DB=petclinic \
  -e POSTGRES_PASSWORD=petclinic \
  -v "$SCRIPT_PATH/src/main/resources/db/postgresql":/docker-entrypoint-initdb.d/ \
  -p 5432:5432 \
  postgres:16.0

