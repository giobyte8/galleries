#!/bin/bash

GL_VERSION=1.0.0

docker build -t "giobyte8/galleries:${GL_VERSION}" -f galleries.dockerfile ..
docker build -t "giobyte8/galleries-downloader:${GL_VERSION}" -f downloader.dockerfile ..
