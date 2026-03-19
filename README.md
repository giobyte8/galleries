# Scanner
Indexes gallery directories and images by scanning a local media library.

- [Architecture](docs/architecture.md)
- [Development](docs/DEVELOPMENT.md)

## What it does

This service:

- stores managed directories in storage
- receives scan requests through RabbitMQ
- scans the local filesystem for image files
- updates the gallery index and requests thumbnail work
- exposes a small HTTP API to browse indexed directories and images

## How it works

1. A directory is registered in storage.
2. A scan request is published to RabbitMQ.
3. The scanner validates the request and walks the target directory.
4. Found images are created or updated in storage.
5. Missing files are removed from the index after reconciliation.
6. Thumbnail generation or deletion is requested through RabbitMQ.

## Triggering a scan

Scan requests are consumed from the queue configured in `galleries.scanner.amqp.queue_scan_requests`.

Expected message shape:

```json
{
  "id": "e4461002-a5ac-4b3a-b050-23a0355f1eaf",
  "path": "relative/path/to/directory",
  "requestedAt": "2025-05-03T10:15:35"
}
```

## Deployment

This service is intended to run as part of the wider Galleries stack.
Use the project docker-compose setup and provide the required environment-specific configuration.

## More details

- Architecture and component/data flow: [docs/architecture.md](docs/architecture.md)
- Local setup, testing, and build notes: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)
