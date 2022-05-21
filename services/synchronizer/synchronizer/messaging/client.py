import contextlib
import json
import pika
from typing import List

import synchronizer.config as cfg
from synchronizer.db.models import DBHttpSource
from synchronizer.sync_logging import logger


_SYNC_HTTP_SOURCE_QUEUE = 'sync_http_source'


@contextlib.contextmanager
def _rb_channel():
    try:
        conn = pika.BlockingConnection(pika.ConnectionParameters(
            host=cfg.rabbitmq_host(),
            port=cfg.rabbitmq_port(),
            credentials=pika.PlainCredentials(
                cfg.rabbitmq_user(),
                cfg.rabbitmq_pass()
            )
        ))

        yield conn.channel()
        conn.close()
    except Exception as e:
        logger.error('RabbitMQ connection failed: %s', e)


def sync_http_sources(sources: List[DBHttpSource]) -> None:
    """Publishes message to request synchronization of each given queue

    Args:
        sources (List[DBHttpSource]): Queues to synchronize, a message \
            will be published for each provided queue
    """
    with _rb_channel() as channel:
        channel.queue_declare(_SYNC_HTTP_SOURCE_QUEUE)

        for source in sources:
            msg = {
                'source_id': str(source.id),
                'url': source.url,
                'content_path': source.content_path
            }

            channel.basic_publish(
                exchange='',
                routing_key=_SYNC_HTTP_SOURCE_QUEUE,
                body=json.dumps(msg)
            )

            logger.info('Source queued for sync: %s', source.id)
