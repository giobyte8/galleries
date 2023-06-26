#!/bin/bash
# Builds and pushes a new 'gl-http_downloader' multi arch docker image
# - Image tag is specified by first argument
# - Second argument must be '--push' in order to build a multiarch image
#   and push it into docker registry
#

IMAGE_NAME=gl-http_downloader
IMAGE_TAG=0
PUSH_IMAGE=false

if [ "$1" = "" ]; then
  echo "Image tag should be specified as argument"
  exit 1
else
  IMAGE_TAG=$1
fi

if [ "$2" = "--push" ]; then
  PUSH_IMAGE=true
fi

if ! [ -f "./http_downloader.dockerfile" ]; then
  echo "Docker file not found: ./http_downloader.dockerfile"
  exit 1
fi

if [ "$PUSH_IMAGE" = true ]; then

  # Multi arch build and push
  docker buildx build                     \
      --platform linux/amd64,linux/arm64  \
      -t giobyte8/"${IMAGE_NAME}":"$IMAGE_TAG" \
      -f http_downloader.dockerfile               \
      --push                              \
      ..

  echo
  echo "Version $IMAGE_TAG of "${IMAGE_NAME}" was released to docker registry"
else

  # Local build (No push to container registry and no multi-arch support)
  docker build                                  \
    -t giobyte8/"${IMAGE_NAME}":"${IMAGE_TAG}"  \
    -f http_downloader.dockerfile                \
    ..

  echo
  echo "Version $IMAGE_TAG of "${IMAGE_NAME}" is ready for local usage"
fi