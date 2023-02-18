
CREATE TABLE content_dir(
    hashed_path VARCHAR(64) PRIMARY KEY,
    path VARCHAR(5000) NOT NULL,
    `recursive` BOOLEAN NOT NULL DEFAULT false,
    `status` VARCHAR(255) NOT NULL,
    last_scan_start DATETIME,
    last_scan_completion DATETIME
);

CREATE TABLE media_file(
    hashed_path VARCHAR(64) PRIMARY KEY,
    path VARCHAR(5000) NOT NULL,
    `hash` VARCHAR(64) NOT NULL,
    `status` VARCHAR(255) NOT NULL,
    datetime_original DATETIME,
    gps_latitude DECIMAL(10, 8),
    gps_longitude DECIMAL(11, 8),
    camera VARCHAR(255)
);

CREATE TABLE dir_contains_mfile(
    id VARCHAR(255) PRIMARY KEY,
    dir_hashed_path VARCHAR(64) NOT NULL,
    file_hashed_path VARCHAR(64) NOT NULL,

    CONSTRAINT fk_dir_hashed__path FOREIGN KEY (dir_hashed_path)
        REFERENCES content_dir(hashed_path),
    CONSTRAINT fk_file_hashed_path FOREIGN KEY (file_hashed_path)
        REFERENCES media_file(hashed_path)
);