#!/bin/bash
# Rollback all db migrations and apply them again. Finally seed
# some development data into DB

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CURR_PATH="${pwd}"

cd $HERE

source .env
MYSQL_HOST=host.docker.internal

echo "Executing rollback of DB migrations"
cd ../db/
./matw rollback

echo ""
echo "Applying migrations"
./matw migrate
cd $HERE

echo
echo "Seeding development data"
docker run --rm -i mysql:8.0.29 mysql \
    -h$MYSQL_HOST \
    -u$MYSQL_USER \
    -p$MYSQL_PASSWORD <./mysql_init_dev.sql

echo
echo "Database is ready for development"

cd $CURR_PATH