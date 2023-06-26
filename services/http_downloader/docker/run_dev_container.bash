# Use this script for development purposes only.
#
# For production deployment use the compose file that includes all of
# the 'galleries' microservices
#

IMAGE_NAME=gl-http_downloader
IMAGE_TAG=local-testing

HOST_CONTENT_DIR=/Users/giovanni.aguirre/src/python/galleries/data.dev/galleries
HOST_RUNTIME_DIR=/Users/giovanni.aguirre/src/python/galleries/services/http_downloader/data.dev/runtime

docker run -ti --rm                                          \
  --name http_downloader                                     \
  --env-file ./http_downloader.docker.env                     \
  --volume "${HOST_CONTENT_DIR}":/opt/galleries/content_dir  \
  --volume "${HOST_RUNTIME_DIR}":/opt/galleries/runtime_dir  \
  giobyte8/"${IMAGE_NAME}":"${IMAGE_TAG}"
