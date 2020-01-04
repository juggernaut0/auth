#!/bin/env bash
set -e

docker run --name auth-db --rm -v "$PWD/init_db.sql:/docker-entrypoint-initdb.d/init_db.sql:ro" -d -p 6432:5432 postgres:11
echo "Postgres started with name 'auth-db'"
sleep 5
./gradlew :dbmigrate:run
