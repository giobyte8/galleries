#!/bin/bash
# Wipes db data and start a fresh database in a fresh container

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CURR_PATH="${pwd}"

source .env
MYSQL_HOST=host.docker.internal


cd $HERE

echo "Stopping database container"
docker-compose stop mysql

echo
echo "Deleting mysql data"
rm -rf ../data.dev/mysql

echo
echo "Starting a fresh mysql container"
docker-compose up -d mysql

echo
echo "Let's give 1 min to mysql for starting up ⏰"
echo "..."
sleep 60

echo
echo "Applying database migrations"
cd ../db
./matw migrate

echo
echo "Seeding development data"
cd $HERE
docker run --rm -i mysql:8.0.29 mysql \
    -h$MYSQL_HOST \
    -u$MYSQL_USER \
    -p$MYSQL_PASSWORD <./mysql_init_dev.sql

echo
echo "Fresh database is ready"

cd $CURR_PATH