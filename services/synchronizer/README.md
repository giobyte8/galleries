# Synchronizer
Takes care of orchestrating several tasks across galleries microservices
such as:
- Sources content synchronization
- Content directories scanning

## Contents
- [How it works](#how-it-works)
- [AMQP emitted events](#amqp-emitted-events)
- [Deployment](#deployment)
- [Development](#development)

## How it works
There are two phases in the synchronization process

1. Initiate sources sync.

   There is an included command `synchronizer/cmd_sync_sources.py` that will
   produce AMQP messages requesting content synchronization for all sources
   found in db.

   Such command is executed on a configurable schedule to sync sources on a
   regular basis.

2. Process sync events.

   As part of sources synchronization several AMQP messages are produced. Such
   messages are consumed and used to post other AMQP messages to different
   microservices in order to execute subsequent tasks such as thumbnails
   generation or content metadata scanning.

## AMQP emitted events

- [Event: Source sync order](#event-source-sync-order)

### Event: Source sync order
Source synchronization request are sent to corresponding service through AMQP.

The queue name is configurable through `.env` variables
```shell
AMQP_Q_SYNC_HTTP_SRC_ORDERS=GL_SYNC_HTTP_SOURCE_ORDERS
```

Message content is in JSON format, as in following example:
```json
{
    "source_id": 77735,
    "url": "https://unsplash.com/collections/72573628/portraits",
    "content_path": "portraits/"
}
```

Where:
- `source_id`: Database id of source to synchronize
- `url`: Source content's url (Where to fetch images from)
- `content_path`: Where to store downloaded content. Path is relative to value
   of `CONTENT_ROOT` in `.env` file.

## Deployment
The recommended way to deploy is by using the `docker-compose.yml` file
provided as part of the whole project. Such file includes all the galleries
microservices.

See the [galleries deployment guide](../../README.md#synchronizer)
for a full walkthrough.

## Development

See [development section](./DEVELOPMENT.md)
