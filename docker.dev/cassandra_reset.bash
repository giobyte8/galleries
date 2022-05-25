#!/bin/bash
# Wipes db data and start a fresh database in a fresh container

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CURR_PATH="${pwd}"

cd $HERE

echo "Stopping cassandra container"
docker-compose stop cassandra

echo
echo "Deleting cassandra data"
rm -rf ../data.dev/cassandra

echo
echo "Starting a fresh cassandra container"
docker-compose up -d cassandra

echo
echo "Let's give 2 mins to cassandra for starting up ⏰"
echo "..."
sleep 120

echo
echo "Seeding database"
./cassandra_init.bash

echo
echo "Fresh database is ready"

cd $CURR_PATH

