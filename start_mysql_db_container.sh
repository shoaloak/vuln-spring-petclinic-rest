#!/bin/bash

# Get absolute script path
readonly SCRIPT_PATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

docker run \
  --rm \
  --detach \
  --name mysql-petclinic \
  -e MYSQL_ROOT_PASSWORD=petclinic \
  -e MYSQL_DATABASE=petclinic \
  -v "$SCRIPT_PATH/src/main/resources/db/mysql":/docker-entrypoint-initdb.d/ \
  -p 3306:3306 \
  mysql:8.0
