-- Seeds development data into database
USE gl;

INSERT INTO http_source(id, url, content_path, sync_remote_deletes) VALUES
    (NULL, 'https://unsplash.com/collections/72573628/portraits', '/portraits', true),
    (NULL, 'https://unsplash.com/collections/xcwlXABKFeo/cities-%26-towns', '/cities_towns', false);


INSERT INTO content_dir(hashed_path, `path`, `recursive`, `status`) VALUES
    ('ae44596c94bfd98471dd2da440b699d6cfd1570f4f03b9c55acedfeb5ca71fdc', 'testphotos/', false, 'SCAN_PENDING');
