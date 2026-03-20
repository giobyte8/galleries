# Produces an AMQP message requesting a given
# gallery to be scanned
#
set -e

function json_escape() {
  printf '%s' "$1" | python -c 'import json,sys; print(json.dumps(sys.stdin.read()))'
}

# ref: https://stackoverflow.com/a/4774063/3211029
SCRIPT_PATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CALLER_PATH="$(pwd)"
cd "$SCRIPT_PATH"

# Source .env file if exists
if [ -f .env ]; then

  # Following variables are expected to be set in .env:
  #   RABBITMQ_HOST, RABBITMQ_USER, RABBITMQ_PASS,
  #   AMQP_EXCHANGE, AMQP_QUEUE_SCAN_REQUESTS
  source .env
else
  echo ".env file not found. Exiting."
  exit 1
fi

RABBITMQ_API_PORT=15672

# Message payload
UUID=$(uuidgen)
REQUESTED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
msg="{
  \"id\": \"$UUID\",
  \"path\": \"Wallpapers\",
  \"requestedAt\": \"$REQUESTED_AT\"
}"
j_msg=$(json_escape "$msg")

amqp_msg="{
  \"properties\": {\"content_type\": \"application/json\"},
  \"routing_key\": \"$AMQP_QUEUE_SCAN_REQUESTS\",
  \"payload\": $j_msg,
  \"payload_encoding\": \"string\"
}"

# Post message to RabbitMQ
echo "Posting scan request to RabbitMQ..."
curl -s \
  -u "$RABBITMQ_USER:$RABBITMQ_PASS"  \
  -X POST                                     \
  -d "$amqp_msg"                              \
  "http://$RABBITMQ_HOST:$RABBITMQ_API_PORT/api/exchanges/%2F/$AMQP_EXCHANGE/publish"

cd "$CALLER_PATH"
