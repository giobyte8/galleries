# Due to 'COPY' commands below, build context must be root of
# 'http_downloader' service

FROM python:3.8.17-alpine3.18
# TODO Make user configurable through $UID:$GID (Default to non root?)

# Copy project files into image
WORKDIR /opt/galleries/http_downloader
COPY http_downloader/  /opt/galleries/http_downloader/http_downloader
COPY requirements.txt  /opt/galleries/http_downloader/requirements.txt

# Install dependencies and run app
RUN apk add --no-cache tzdata && pip install -r requirements.txt
ENTRYPOINT ["python", "http_downloader/downloader.py"]
