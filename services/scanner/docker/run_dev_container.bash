#
# Use this script for development purposes only.
# For production deployment use the compose file that includes all of
# the 'galleries' microservices
#

IMAGE_TAG=local-testing
HOST_CONTENT_DIR=/Users/giovanni.aguirre/src/python/galleries/data.dev/galleries

docker run -ti --rm                                          \
  --name scanner01                                           \
  --env-file ./scanner.env                                   \
  --volume "${HOST_CONTENT_DIR}":/opt/galleries/content_dir  \
  giobyte8/gl-scanner:"${IMAGE_TAG}"
