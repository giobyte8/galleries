#!/usr/bin/env bash
# Smoke-check a freshly built galleries image.
#
# Validation steps:
# - Prepare env dependencies for the app:
#   - Neo4j ephemeral container
#   - RabbitMQ ephemeral container
# - Start app container
# - Verify app health via actuator health endpoint
# - Verify 'exiftool' command is available in the runtime image
#
# Usage:
#   ./image_verify.bash <image_ref>
#
# Optional env vars:
#   SMOKE_PORT=18100
#   SMOKE_START_TIMEOUT_SEC=180
#   SMOKE_POLL_INTERVAL_SEC=2
#   SMOKE_NEO4J_IMAGE=neo4j:5.20-community-bullseye
#   SMOKE_RABBITMQ_IMAGE=rabbitmq:3.13-alpine

set -euo pipefail

IMAGE_REF="${1:-}"
if [ -z "$IMAGE_REF" ]; then
  echo "Usage: ./image_verify.bash <image_ref>"
  exit 1
fi

SMOKE_PORT="${SMOKE_PORT:-18100}"
START_TIMEOUT_SEC="${SMOKE_START_TIMEOUT_SEC:-180}"
POLL_INTERVAL_SEC="${SMOKE_POLL_INTERVAL_SEC:-2}"
NEO4J_IMAGE="${SMOKE_NEO4J_IMAGE:-neo4j:5.20-community-bullseye}"
RABBITMQ_IMAGE="${SMOKE_RABBITMQ_IMAGE:-rabbitmq:3.13-alpine}"

RUN_ID="${RANDOM}"
NETWORK_NAME="galleries-smoke-net-${RUN_ID}"
APP_CONTAINER="galleries-smoke-app-${RUN_ID}"
NEO4J_CONTAINER="galleries-smoke-neo4j-${RUN_ID}"
RABBITMQ_CONTAINER="galleries-smoke-rabbit-${RUN_ID}"
HEALTH_URL="http://127.0.0.1:${SMOKE_PORT}/actuator/health"

cleanup() {
  docker rm -f "$APP_CONTAINER" >/dev/null 2>&1 || true
  docker rm -f "$NEO4J_CONTAINER" >/dev/null 2>&1 || true
  docker rm -f "$RABBITMQ_CONTAINER" >/dev/null 2>&1 || true
  docker network rm "$NETWORK_NAME" >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "Creating smoke-check network: ${NETWORK_NAME}"
docker network create "$NETWORK_NAME" >/dev/null

echo "Starting Neo4j dependency: ${NEO4J_CONTAINER} (${NEO4J_IMAGE})"
docker run -d \
  --name "$NEO4J_CONTAINER" \
  --network "$NETWORK_NAME" \
  --network-alias neo4j \
  -e NEO4J_AUTH=neo4j/smokepass \
  "$NEO4J_IMAGE" >/dev/null

echo "Starting RabbitMQ dependency: ${RABBITMQ_CONTAINER} (${RABBITMQ_IMAGE})"
docker run -d \
  --name "$RABBITMQ_CONTAINER" \
  --network "$NETWORK_NAME" \
  --network-alias rabbitmq \
  -e RABBITMQ_DEFAULT_USER=smoke \
  -e RABBITMQ_DEFAULT_PASS=smoke \
  "$RABBITMQ_IMAGE" >/dev/null

echo "Waiting for Neo4j readiness"
start_ts="$(date +%s)"
while true; do
  if docker exec "$NEO4J_CONTAINER" \
      cypher-shell -a bolt://localhost:7687 -u neo4j -p smokepass \
      'RETURN 1' >/dev/null 2>&1; then
    break
  fi

  now_ts="$(date +%s)"
  if [ "$((now_ts - start_ts))" -ge "$START_TIMEOUT_SEC" ]; then
    echo "Neo4j did not become ready within ${START_TIMEOUT_SEC}s"
    docker logs "$NEO4J_CONTAINER" || true
    exit 1
  fi

  sleep "$POLL_INTERVAL_SEC"
done

echo "Waiting for RabbitMQ readiness"
start_ts="$(date +%s)"
while true; do
  if docker exec "$RABBITMQ_CONTAINER" \
      rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
    break
  fi

  now_ts="$(date +%s)"
  if [ "$((now_ts - start_ts))" -ge "$START_TIMEOUT_SEC" ]; then
    echo "RabbitMQ did not become ready within ${START_TIMEOUT_SEC}s"
    docker logs "$RABBITMQ_CONTAINER" || true
    exit 1
  fi

  sleep "$POLL_INTERVAL_SEC"
done

echo
echo "Starting app container: ${APP_CONTAINER} from ${IMAGE_REF}"
docker run -d \
  --name "$APP_CONTAINER" \
  --network "$NETWORK_NAME" \
  -p "${SMOKE_PORT}:8100" \
  -e RABBITMQ_HOST=rabbitmq \
  -e RABBITMQ_PORT=5672 \
  -e RABBITMQ_USERNAME=smoke \
  -e RABBITMQ_PASSWORD=smoke \
  -e NEO4J_HOST=neo4j \
  -e NEO4J_PORT=7687 \
  -e NEO4J_USERNAME=neo4j \
  -e NEO4J_PASSWORD=smokepass \
  "$IMAGE_REF" >/dev/null

echo "  Waiting for actuator health endpoint (${HEALTH_URL})"
start_ts="$(date +%s)"
spinner_chars='|/-\\'
spin_idx=0
while true; do
  # Use a silent curl and suppress stderr so transient connection errors
  # don't flood the logs. Inspect the body for a concrete UP status value.
  body="$(curl --silent --fail "$HEALTH_URL" 2>/dev/null || true)"
  if [ -n "$body" ] && echo "$body" | grep -q '"status":"UP"'; then
    echo
    echo "App is running healthy"
    break
  fi

  now_ts="$(date +%s)"
  elapsed="$((now_ts - start_ts))"
  if [ "$elapsed" -ge "$START_TIMEOUT_SEC" ]; then
    echo "Container did not become healthy within ${START_TIMEOUT_SEC}s"
    echo "--- app container logs ---"
    docker logs "$APP_CONTAINER" || true
    exit 1
  fi

  if ! docker ps --format '{{.Names}}' | grep -qx "$APP_CONTAINER"; then
    echo "Container exited before becoming healthy"
    echo "--- app container logs ---"
    docker logs "$APP_CONTAINER" || true
    echo "--- neo4j container logs ---"
    docker logs "$NEO4J_CONTAINER" || true
    echo "--- rabbitmq container logs ---"
    docker logs "$RABBITMQ_CONTAINER" || true
    exit 1
  fi

  # Friendly, in-place progress indicator to avoid log spam
  now_ts="$(date +%s)"
  elapsed_disp="$((now_ts - start_ts))"

  # pick spinner char
  ch="${spinner_chars:spin_idx%${#spinner_chars}:1}"

  # Print on same line (carriage return), clear line and show elapsed seconds
  printf "\r  Not yet ready... %s (%ds)" "$ch" "$elapsed_disp"
  spin_idx=$((spin_idx + 1))

  sleep "$POLL_INTERVAL_SEC"
done
printf "\n"

echo "Verifying exiftool availability..."
docker exec "$APP_CONTAINER" exiftool -ver >/dev/null

echo
echo "Image verification passed for ${IMAGE_REF}"
