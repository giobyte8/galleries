import uuid
from cassandra.cqlengine import columns, connection
from cassandra.cqlengine.models import Model


_GALLERIES_KEYSPACE = 'gl'
connection.setup(['127.0.0.1'], _GALLERIES_KEYSPACE)

class DBHttpSource(Model):
    __table_name__ = 'http_source'

    id = columns.UUID(primary_key=True, default=uuid.uuid4())
    url = columns.Text(required=True)
    content_path = columns.Text(required=True)
    sync_remote_deletes = columns.Boolean(required=True, default=True)
