# Puts app and run configs into a python alpine based image
#
# Note for building:
# Build context should be project root path
# (Check COPY command below)
#

FROM python:3.9-alpine3.15

WORKDIR /opt/galleries
COPY galleries/ /opt/galleries/galleries/
COPY requirements.txt /opt/galleries/

RUN apk add --no-cache tzdata && pip install -r requirements.txt
