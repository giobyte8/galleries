
CREATE TABLE http_source(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    url VARCHAR(5000) NOT NULL,
    content_path VARCHAR(5000) NOT NULL,
    sync_remote_deletes BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE http_source_item(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    source_id INTEGER NOT NULL,
    `filename` VARCHAR(1000) NOT NULL,
    remote_status VARCHAR(255) NOT NULL,

    CONSTRAINT fk_source FOREIGN KEY (source_id)
        REFERENCES http_source(id)
);
