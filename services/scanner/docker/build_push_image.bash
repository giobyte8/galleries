#!/bin/bash
#
# Builds and pushes a new 'gl-scanner' multi arch docker image
# Image tag is specified by first argument
#

IMAGE_TAG=0

if [ "$1" = "" ]; then
  echo "Image tag should be specified as argument"
  exit 1
else
  IMAGE_TAG=$1
fi

if ! [ -f "./scanner.dockerfile" ]; then
  echo "Docker file not found: ./scanner.dockerfile"
  exit 1
fi

# Local build (No push to container registry)
#docker build -t giobyte8/gl-scanner:"${IMAGE_TAG}" -f scanner.dockerfile ..

# Multi arch build and push
docker buildx build                     \
    --platform linux/amd64,linux/arm64  \
    -t giobyte8/gl-scanner:"$IMAGE_TAG" \
    -f scanner.dockerfile               \
    --push                              \
    ..

echo
echo "Version $IMAGE_TAG of gl-scanner was released to docker registry"