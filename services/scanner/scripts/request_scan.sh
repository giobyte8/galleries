# Produces an AMQP message requesting a given
# gallery to be scanned

function json_escape() {
  printf '%s' "$1" | python -c 'import json,sys; print(json.dumps(sys.stdin.read()))'
}

# ref: https://stackoverflow.com/a/4774063/3211029
SCRIPT_PATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CALLER_PATH="$(pwd)"
cd "$SCRIPT_PATH"

# Prepare env
#source ../.env
RABBITMQ_USER=
RABBITMQ_PASS=

RABBITMQ_API_PORT=15672
AMQP_EXCHANGE=GL_EXCHANGE
AMQP_ROUTING_KEY=GL_SCAN_REQUESTS

# Message payload
UUID=$(uuidgen)
msg="{
  \"id\": \"$UUID\",
  \"dirPath\": \"inspiring_digital_art\",
  \"requestedAt\": \"2025-05-03T10:15:35\"
}"
j_msg=$(json_escape "$msg")

amqp_msg="{
  \"properties\": {},
  \"routing_key\": \"$AMQP_ROUTING_KEY\",
  \"payload\": $j_msg,
  \"payload_encoding\": \"string\"
}"

# Post message to RabbitMQ
echo "Posting scan request to RabbitMQ..."
curl -s \
  -u "$RABBITMQ_USER:$RABBITMQ_PASS"  \
  -X POST                                     \
  -d "$amqp_msg"                              \
  http://localhost:$RABBITMQ_API_PORT/api/exchanges/%2F/$AMQP_EXCHANGE/publish

cd "$CALLER_PATH"
