# Puts the synchronizer service into a python alpine
# based images
#
# Note: Build context should be the whole project root,
# check the below COPY commands
#

FROM python:3.8.13-alpine3.16

WORKDIR /opt/synchronizer
COPY services/synchronizer /opt/synchronizer/

RUN apk add --no-cache build-base tzdata supercronic libffi-dev && \
    pip install -r requirements.txt
