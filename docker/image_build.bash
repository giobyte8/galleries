#!/usr/bin/env bash
# Builds the docker image and runs a test container to smoke check the image.
# Usage:
#   ./image_build.bash <image_tag> [--push] [--skip-check]
#
# Arguments:
#  - <image_tag> Defaults to 'dev'.
#  - --push: If specified, build image for multiplatform (amd64 and arm64) via
#      'buildx' and pushes it into docker registry.
#  - --skip-check: Skips the smoke testing of the built image.
#

set -euo pipefail

SCRIPT_PATH="$(cd -- "$(dirname "$0")" >/dev/null 2>&1; pwd -P)"
cd "$SCRIPT_PATH"

DOCKER_FILE="./galleries.dockerfile"
IMAGE="giobyte8/galleries"
IMAGE_TAG="dev"
PUSH_IMAGE=false
SKIP_CHECK=false

for arg in "$@"; do
  case "$arg" in
    --push)
      PUSH_IMAGE=true
      ;;
    --skip-check)
      SKIP_CHECK=true
      ;;
    --*)
      echo "Unknown argument: $arg"
      exit 1
      ;;
    *)
      IMAGE_TAG="$arg"
      ;;
  esac
done

if [ ! -f "${DOCKER_FILE}" ]; then
  echo "Docker file not found: ${DOCKER_FILE}"
  exit 1
fi

IMAGE_REF="${IMAGE}:${IMAGE_TAG}"

echo "Building local image: ${IMAGE_REF}"
docker build \
  -t "$IMAGE_REF" \
  -f "${DOCKER_FILE}" \
  ..

if [ "$SKIP_CHECK" = false ]; then
  echo
  echo "Running smoke check for: ${IMAGE_REF}"
  ./image_verify.bash "$IMAGE_REF"
fi

if [ "$PUSH_IMAGE" = true ]; then
  echo
  echo "Publishing multi-arch image: ${IMAGE_REF}"
  docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t "$IMAGE_REF" \
    -f "${DOCKER_FILE}" \
    --push \
    ..

  echo
  echo "Version ${IMAGE_TAG} of ${IMAGE} was released to docker registry"
else
  echo
  echo "Version ${IMAGE_TAG} of ${IMAGE} is ready for local usage"
fi
