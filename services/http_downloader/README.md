# http_downloader
Synchronizes remote content sources with local content directories

- [How it works](#how-it-works)
  - [How to trigger source synchronization](#how-to-trigger-source-synchronization)
  - [AMQP Events emitted during sync](#amqp-events-emmited-during-sync)
- [Deployment](#deployment)
- [Development](#development)

## How it works
It uses [gallery-dl](https://github.com/mikf/gallery-dl) under the hood to
fetch content from remote sources.

Every downloaded file is registered into database in order to keep a record and
allow synchronization of deleted files in remote sources.

1. It receives a 'sync source' request through AMQP
2. A configuration file for `gallery-dl` is prepared for this specific source
   including following configurations:
   1. Hook to invoke upon new file downloaded
   2. Hook to invoke upon file download skipped (Previously downloaded)
   3. Cookies that may be required by remote sites to allow content download
3. For every downloaded file a corresponding hook (python script) is executed
   and a message is posted to AMQP
4. For every skipped file (Previously downloaded) a corresponding hook
   (python script) is executed and a message is posted to AMQP
5. Finally, an AMQP message is posted to notify that given source has been
   synchronized

### How to trigger source synchronization
Sync requests are received through AMQP. Broker connection params are
configured in `.env` file.

Queue name is configured in `.env` file
```shell
AMQP_Q_SYNC_HTTP_SRC_ORDERS=GL_SYNC_HTTP_SOURCE_ORDERS
```

AMQP message must be a JSON object like:
```json
{
  "source_id": 1,
  "url": "https://unsplash.com/collections/72573628/portraits",
  "content_path": "portraits/"
}
```
Where:
- `source_id`: Database id of source to synchronize
- `url`: Source content's url (Where to fetch images from)
- `content_path`: Where to store downloaded content. Path is relative to value
  of `CONTENT_ROOT` in `.env` file.

### AMQP Events emmited during sync
During synchronization, multiple AMQP messages are *produced* to notify about
syncrhonization events.

#### Event: File Downloaded
Posted when a file is downloaded from remote source.

Queue name is configured in `.env` file:
```shell
AMQP_Q_FILE_DOWNLOADED=GL_FILE_DOWNLOADED
```

Message example:
```json
{
    "source_id": 725,
    "filename": "6miu5n4ybt3.jpg"
}
```
Where:
- `source_id`: Database id of source where this file belongs
- `filename`: Name of downloaded file. File absolute location will be
  determined by value of `CONTENT_ROOT` and source `content_path`

#### Event: File download skipped
Posted when a file present in remote source is already present in local
directory (Previously downloaded). Hence, download of such file is skipped

Queue name is configured in `.env` file:
```shell
AMQP_Q_FILE_DOWNLOAD_SKIPPED=GL_FILE_DOWNLOAD_SKIPPED
```

Message example:
```json
{
    "source_id": 725,
    "filename": "6miu5n4ybt3.jpg"
}
```
Where:
- `source_id`: Database id of source where this file belongs
- `filename`: Name of skipped file. File absolute location will be
  determined by value of `CONTENT_ROOT` and source `content_path`

#### Event: Source synchronized
Posted when source synchronization process is complete. All remote files
downloaded to local directory.

Queue name is configured in `.env` file:
```shell
AMQP_Q_SOURCE_SYNCHRONIZED=GL_SOURCE_SYNCHRONIZED
```

Message example:
```json
{
    "source_id": 725
}
```
Where:
- `source_id`: Database id of synchronized source

## Deployment
The recommended way to deploy is by using the docker-compose.yml file provided
as part of the whole project. Such file includes all the galleries
microservices.

See [galleries deployment guide]() for a full walkthrough.

## Development

See [development section](./DEVELOPMENT.md).
