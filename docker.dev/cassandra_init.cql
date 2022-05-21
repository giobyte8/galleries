-- Create development keyspace
CREATE KEYSPACE IF NOT EXISTS gl WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': '1'};

CREATE TABLE IF NOT EXISTS gl.http_source(
    id UUID PRIMARY KEY,
    url TEXT,
    content_path TEXT,
    sync_remote_deletes BOOLEAN
);

-- Should it be only for http source items?
CREATE TABLE IF NOT EXISTS gl.source_item(
    id UUID PRIMARY KEY,
    source_id TEXT,
    path TEXT,
    deleted_on_remote TEXT
);

-- Insert some test data
INSERT INTO gl.http_source(id, url , content_path, sync_remote_deletes) VALUES(uuid(), 'https://unsplash.com/collections/72573628/portraits', '/portraits', true);
INSERT INTO gl.http_source(id, url , content_path, sync_remote_deletes) VALUES(uuid(), 'https://unsplash.com/collections/xcwlXABKFeo/cities-%26-towns', '/cities_towns', true);
