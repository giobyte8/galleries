Look for multimedia files in source directories.

- Scan a given path looking for images and videos
- Extract file metadata (date, camera, latitude, longitude)
- Save path and metadata of each found file
- Queue job for thumbnail generation of each found file

## Design

Scanning of directories starts when service receives an AMQP message. Such message indicates the directory to scan using its id (Directories were previously saved to database).

Before start scanning, service updates directory's status to `SCANNING` to allow other instances and services to know that a scan is in progress.

If directory has been previously scanned, service updates all previously found files status to `IN_REVIEW`, so that they could be updated or removed during scanning process.

Service iterates over all files in directory (Maybe recursively depending on directory config). For each found file:

1. Compute file hash
2. Check if file was previously scanned (Already exists in database).
3. If file record exists in database:
	1. Compare hashes, If they match then mark file as `READY` and finish process for this file
4. Otherwise:
	1. Scan file metadata: Taken date, Geolocation and Camera info.
	2. Save file path, hash and metadata into database. Set its status as `THUMBNAIL_PENDING`
	3. Post AMQP message to request thumbnail generation for file

Once all files have been iterated, update directory files count, last scan date and status to `READY`

### Media directories statuses

- `SCAN_PENDING` Initial status upon registration
- `SCAN_IN_PROGRESS`
- `SCAN_COMPLETE`

### Media files statuses

* `THUMBNAILS_PENDING` Initial status upon registration
* `THUMBNAILS_IN_PROGRESS`
* `IN_REVIEW` Indicates a scan over its parent directory to verify this file
* `READY` Media file is ready for usage by galleries