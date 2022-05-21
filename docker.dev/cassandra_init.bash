#!/bin/bash
# Initializes cassandra with data for development

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

SCRIPT="${HERE}/cassandra_init.cql"
HOST=host.docker.internal
PORT=9042

echo "Connecting to $HOST:$PORT"
echo "Executing file: $SCRIPT"
docker run --rm -it \
    -v "$SCRIPT:/scripts/init.cql" \
    nuvo/docker-cqlsh cqlsh $HOST $PORT \
        --cqlversion='3.4.5' \
        -f /scripts/init.cql