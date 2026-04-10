#!/usr/bin/env bash
# Reset dev Neo4j data and seed dynamic development directories.
set -euo pipefail

# ref: https://stackoverflow.com/a/4774063/3211029
SCRIPT_PATH="$(cd -- "$(dirname "$0")" >/dev/null 2>&1; pwd -P)"
CALLER_PATH="$(pwd)"
cd "$SCRIPT_PATH"

if [ -f .env ]; then
  source .env
else
  echo ".env file not found. Exiting."
  exit 1
fi

NEO4J_HOST="${NEO4J_HOST:-host.docker.internal}"
NEO4J_PORT="${NEO4J_PORT:-7687}"
NEO4J_USERNAME="${NEO4J_USERNAME:-neo4j}"
NEO4J_PASSWORD="${NEO4J_PASSWORD:-}"
NEO4J_DATABASE="${NEO4J_DATABASE:-neo4j}"
NEO4J_SHELL_IMAGE="${NEO4J_SHELL_IMAGE:-neo4j:5.26}"
DEV_SCAN_DIRS="${DEV_SCAN_DIRS:-}"
DRY_RUN=false

for arg in "$@"; do
  case "$arg" in
    --dry-run)
      DRY_RUN=true
      ;;
    *)
      echo "Unknown argument: $arg"
      echo "Usage: ./dev_data_reset.sh [--dry-run]"
      exit 1
      ;;
  esac
done

CYPHER_TEMPLATE="dev_data_reset.cypher"
MARKER="// __DIR_MERGES__"
TMP_CYPHER=""

cleanup() {
  if [ -n "$TMP_CYPHER" ] && [ -f "$TMP_CYPHER" ]; then
    rm -f "$TMP_CYPHER"
  fi
  cd "$CALLER_PATH"
}
trap cleanup EXIT

if [ -z "$NEO4J_PASSWORD" ]; then
  echo "NEO4J_PASSWORD is required in scripts/.env"
  exit 1
fi

if [ ! -f "$CYPHER_TEMPLATE" ]; then
  echo "Template file not found: $CYPHER_TEMPLATE"
  exit 1
fi

if ! grep -Fqx "$MARKER" "$CYPHER_TEMPLATE"; then
  echo "Template marker not found in $CYPHER_TEMPLATE"
  exit 1
fi

if [ -z "$DEV_SCAN_DIRS" ]; then
  echo "DEV_SCAN_DIRS is required in scripts/.env"
  exit 1
fi

DIR_MERGES=""
DIR_COUNT=0
while IFS= read -r raw_dir; do
  dir=$(printf '%s' "$raw_dir" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')

  if [ -z "$dir" ]; then
    continue
  fi

  if [[ "$dir" == \#* ]]; then
    continue
  fi

  dir_escaped=${dir//\'/\\\'}
  dir_uuid=$(uuidgen | tr '[:upper:]' '[:lower:]')
  DIR_COUNT=$((DIR_COUNT + 1))

  DIR_MERGES+="MERGE (dir${DIR_COUNT}:Directory {\n"
  DIR_MERGES+="    id: '${dir_uuid}',\n"
  DIR_MERGES+="    path: '${dir_escaped}',\n"
  DIR_MERGES+="    recursive: true,\n"
  DIR_MERGES+="    version: 0,\n"
  DIR_MERGES+="    status: 'SCAN_PENDING'\n"
  DIR_MERGES+="});\n\n"
done <<< "$DEV_SCAN_DIRS"

if [ "$DIR_COUNT" -eq 0 ]; then
  echo "No valid entries found in DEV_SCAN_DIRS"
  exit 1
fi

TMP_CYPHER=$(mktemp "$SCRIPT_PATH/.dev_data_reset.XXXXXX.cypher")
while IFS= read -r line || [ -n "$line" ]; do
  if [ "$line" = "$MARKER" ]; then
    printf "%b" "$DIR_MERGES" >> "$TMP_CYPHER"
  else
    printf "%s\n" "$line" >> "$TMP_CYPHER"
  fi
done < "$CYPHER_TEMPLATE"

echo "Resetting dev database at ${NEO4J_HOST}:${NEO4J_PORT}/${NEO4J_DATABASE}"
echo "Seeding ${DIR_COUNT} directories from DEV_SCAN_DIRS"

if [ "$DRY_RUN" = true ]; then
  echo
  echo "*** --dry-run enabled. Generated Cypher:"
  echo
  cat "$TMP_CYPHER"
  echo "** Dry run completed. No DB changes were applied."
  exit 0
fi

docker run --rm \
  -v "$SCRIPT_PATH:/work:ro" \
  "$NEO4J_SHELL_IMAGE" \
  cypher-shell \
  -a "bolt://${NEO4J_HOST}:${NEO4J_PORT}" \
  -u "$NEO4J_USERNAME" \
  -p "$NEO4J_PASSWORD" \
  -d "$NEO4J_DATABASE" \
  -f "/work/$(basename "$TMP_CYPHER")"

echo "Dev database reset completed."

