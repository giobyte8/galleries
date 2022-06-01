# Puts http_downloader service into a python alpine
# based image
#
# Note for building:
# Build context should be whole project root path
# (Check COPY command below)
#

FROM python:3.8.13-alpine3.16

WORKDIR /opt/http_downloader
COPY services/http_downloader /opt/http_downloader/

RUN apk add --no-cache tzdata && pip install -r requirements.txt
CMD ["python", "http_downloader/downloader.py"]
