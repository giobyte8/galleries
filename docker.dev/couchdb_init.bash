#
# From docs at: https://hub.docker.com/_/couchdb, the system databases needs
# to be created manually upon first run.
#
# Use this script for creation of those databases after container first
# startup
#

# Load user and pass variables from env
source .env

curl curl -X PUT "http://${COUCHDB_USER}:${COUCHDB_PASS}@localhost:5984/_users"
curl curl -X PUT "http://${COUCHDB_USER}:${COUCHDB_PASS}@localhost:5984/_replicator"
curl curl -X PUT "http://${COUCHDB_USER}:${COUCHDB_PASS}@localhost:5984/_global_changes"
