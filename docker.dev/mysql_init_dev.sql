-- Seeds development data into database
USE gl;

INSERT INTO http_source(id, url, content_path, sync_remote_deletes) VALUES
    (NULL, 'https://unsplash.com/collections/72573628/portraits', '/portraits', true),
    (NULL, 'https://unsplash.com/collections/xcwlXABKFeo/cities-%26-towns', '/cities_towns', false);