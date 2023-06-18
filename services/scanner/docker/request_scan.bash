#
# Publish a scan request for testing during development

function json_escape() {
  printf '%s' "$1" | python -c 'import json,sys; print(json.dumps(sys.stdin.read()))'
}

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CURR_PATH="${pwd}"
cd $HERE

source scanner.env
RABBITMQ_API_PORT=15672
RABBITMQ_EXCHANGE=GL_EXCHANGE
RABBITMQ_ROUTING_KEY=GL_SCAN_ORDERS
UUID=$(uuidgen)

msg="{
  \"id\": \"$UUID\",
  \"dirHPath\": \"ae44596c94bfd98471dd2da440b699d6cfd1570f4f03b9c55acedfeb5ca71fdc\",
  \"requestedAt\": \"2050-02-14\"
}"
j_msg=$(json_escape "$msg")

amqp_msg="{
  \"properties\": {},
  \"routing_key\": \"$RABBITMQ_ROUTING_KEY\",
  \"payload\": $j_msg,
  \"payload_encoding\": \"string\"
}"

# Post message to RabbitMQ
echo "Posting message to rabbitmq..."
curl -s \
  -u "$RABBITMQ_USERNAME:$RABBITMQ_PASSWORD"  \
  -X POST                                     \
  -d "$amqp_msg"                              \
  http://localhost:$RABBITMQ_API_PORT/api/exchanges/%2F/$RABBITMQ_EXCHANGE/publish
