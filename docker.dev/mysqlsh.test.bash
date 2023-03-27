#!/bin/bash
# Creates a container running mysql client and connects to MySQL integration
# tests instance

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CURR_PATH="${pwd}"

cd $HERE

source .env
TEST_MYSQL_HOST=host.docker.internal

docker run --rm -it mysql mysql -h$TEST_MYSQL_HOST -P3307 -u$TEST_MYSQL_USER -p$TEST_MYSQL_PASSWORD

cd $CURR_PATH
