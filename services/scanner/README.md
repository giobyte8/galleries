# Scanner
Scans directories looking for media files

- [How it works](#how-it-works)
  - [How to trigger directory scan](#how-to-trigger-directory-scan)
  - [Events emitted during scan](#events-emitted-during-scan-of-target-dir)
    - [Scan start and end time](#scan-processing-start-and-end-time)
    - [Scanned file events](#scanned-files-event)
- [Deployment](#deployment)
- [Development](#development)

## How it works

1. Receives scan request through AMQP
   1. Validates request contains a valid target directory
   2. Validates there isn't another scan in progress for same directory
2. Mark all previously found files in target directory as in `VERIFYING` status
3. Scan for files in target dir
   1. Upsert every found file into database along with its metadata
   2. Emit AMQP event for each found file
4. Remove all files that remain in `VERIFYING` status once scan is
   complete (Files not found in dir anymore)
   1. Emit AMQP event for each file deleted from DB

### How to trigger directory scan

Scan requests are received through AMQP.

Connection and queue name params are configured through application properties

````yml
galleries:
  scanner:
    amqp:
      exchange_gl: GL_EXCHANGE
      queue_scan_requests: GL_SCAN_REQUESTS
````

Message must be a JSON object with a structure like following:

```json
{
   "id": "e4461002-a5ac-4b3a-b050-23a0355f1eaf",
   "dirPath": "relative/path/to/directory",
   "requestedAt": "2023-02-14 05:36:23"
}
```

- `id` UUID v4 used to identify the scan request through logs and metrics
- `dirPath` Relative path of directory to scan, such directory must exist in database to enable scanning on it.
- `requestedAt` UTC datetime when this scan request was created by requester.

### Events emitted during scan of target dir

During scan process multiple AMQP messages are emitted to notify about events

#### Scan processing start and end time

Event is posted to following queue:
```yml
galleries:
  scanner:
    amqp:
      queue_scan_hooks: GL_SCAN_HOOKS
```

Scan start message:
```json
{
   "scanRequestId": "e4461002-a5ac-4b3a-b050-23a0355f1eaf",
   "eventType": "SCAN_START",
   "time": "2023-06-09 17:30:35"
}
```

Scan end message:
```json
{
   "scanRequestId": "e4461002-a5ac-4b3a-b050-23a0355f1eaf",
   "eventType": "SCAN_END",
   "time": "2023-06-09 17:35:35"
}
```

Where:
- `scanRequestId` UUID of scan request
- `eventType`: Type of scan event
- `time`: Timestamp when event occurred

#### Scanned files event

During a directory scan several events are emitted:
- `NEW_FILE_FOUND`: When a new file is found in directory
- `FILE_CHANGED`: Content's of previously scanned file have changed
- `FILE_NOT_FOUND`: A previously scanned file does not exist anymore

Events are posted to following queue:
```yml
galleries:
  scanner:
    amqp:
      queue_scan_discovered_files: GL_SCAN_DISCOVERED_FILES
```

Example message:
```json
{
   "scanRequestId": "e4461002-a5ac-4b3a-b050-23a0355f1eaf",
   "eventType": "FILE_CHANGED",
   "filePath": "/testphotos/file.jpg"
}
```

Where:
- `scanRequestId`: UUID of scan request
- `eventType`: One of: 'NEW_FILE_FOUND', 'FILE_CHANGED' or 'FILE_NOT_FOUND'
- `filePath`: Relative path to target file

## Deployment

The recommended way to deploy is by using the `docker-compose.yml` file
provided as part of the whole project. Such file includes all the
galleries microservices.

Make sure to edit the global `.env` file to reference to your target env
and start scanner using docker compose.

See [galleries deployment guide]() for a full walkthrough

## Development
See [development section](./DEVELOPMENT.md)
