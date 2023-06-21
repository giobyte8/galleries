#
# Publish a download/sync request for testing during development

function json_escape() {
  printf '%s' "$1" | python -c 'import json,sys; print(json.dumps(sys.stdin.read()))'
}

# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CURR_PATH="${pwd}"
cd $HERE

source ../.env
RABBITMQ_API_PORT=15672
RABBITMQ_EXCHANGE=GL_EXCHANGE
UUID=$(uuidgen)

msg="{
  \"source_id\": 1,
  \"url\": \"https://unsplash.com/collections/72573628/portraits\",
  \"content_path\": \"portraits/\"
}"
j_msg=$(json_escape "$msg")

amqp_msg="{
  \"properties\": {},
  \"routing_key\": \"$AMQP_Q_SYNC_HTTP_SRC_ORDERS\",
  \"payload\": $j_msg,
  \"payload_encoding\": \"string\"
}"

# Post message to RabbitMQ
echo "Posting sync request to rabbitmq..."
curl -s \
  -u "$RABBITMQ_USER:$RABBITMQ_PASS"  \
  -X POST                                     \
  -d "$amqp_msg"                              \
  http://localhost:$RABBITMQ_API_PORT/api/exchanges/%2F/$RABBITMQ_EXCHANGE/publish

cd $CURR_PATH
