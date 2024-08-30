
# ref: https://stackoverflow.com/a/4774063/3211029
HERE="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
source "$HERE/.env"

echo "Starting sync sources command..."
echo "  OTEL Service name: $OTEL_SERVICE_NAME"
echo

opentelemetry-instrument                                                     \
    --service_name $OTEL_SERVICE_NAME                                        \
    --disabled_instrumentations "${OTEL_PYTHON_DISABLED_INSTRUMENTATIONS}"   \
    python synchronizer/cmd_sync_sources.py
