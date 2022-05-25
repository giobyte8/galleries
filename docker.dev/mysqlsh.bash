#!/bin/bash
# Creates a container running mysql client and connects to cassandra instance

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CURR_PATH="${pwd}"

cd $HERE

source .env
MYSQL_HOST=host.docker.internal

docker run --rm -it mysql mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD

cd $CURR_PATH
