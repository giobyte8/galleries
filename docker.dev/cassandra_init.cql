-- Create development keyspace
CREATE KEYSPACE IF NOT EXISTS gl WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': '1'};

-- Create sources table
CREATE TABLE IF NOT EXISTS gl.source(
    id text PRIMARY KEY,
    url text,
    path text
);

-- Insert some test data
INSERT INTO gl.source(id, url , path) VALUES('1000', 'https://unsplash.com/collections/72573628/portraits', '/portraits');
INSERT INTO gl.source(id, url , path) VALUES('1001', 'https://unsplash.com/collections/xcwlXABKFeo/cities-%26-towns', '/cities_towns');
