#!/bin/bash
# Creates a container running cqlsh and connects to cassandra instance

HOST=host.docker.internal
PORT=9042
docker run --rm -it nuvo/docker-cqlsh cqlsh $HOST $PORT --cqlversion='3.4.5'