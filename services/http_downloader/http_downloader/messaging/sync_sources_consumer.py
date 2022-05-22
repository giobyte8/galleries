import json
import pika
from pika.adapters.blocking_connection import BlockingChannel

import http_downloader.config as cfg
import http_downloader.gdl_wrapper as gdl
from http_downloader.dl_logging import logger
from http_downloader.services.source_service import source_content_abs_path
from http_downloader.messaging import producer


_SYNC_HTTP_SOURCE_QUEUE = 'sync_http_source'


def on_sync_source_message(channel: BlockingChannel, basic_deliver, props, body):
    """Callback invoked by pika when a new messages arrives from \
        _SYNC_HTTP_SOURCE queue

    Args:
        channel (BlockingChannel): pika channel
        basic_deliver (_type_):
        props ():
        body (str): Raw message from queue
    """
    logger.debug(
        'Message received from queue "%s" with body: %s',
        _SYNC_HTTP_SOURCE_QUEUE,
        body.decode('utf-8'))

    # Parse message body
    j_body = json.loads(body.decode('utf-8'))
    content_path = source_content_abs_path(j_body['content_path'])

    # Download source contents
    gdl.download(j_body['source_id'], j_body['url'], content_path)
    channel.basic_ack(basic_deliver.delivery_tag)
    logger.info('Download completed for source: %s', j_body['source_id'])

    # Notify source download completion
    producer.send_source_synchronized(j_body['source_id'])


def start_consumer():
    conn = pika.BlockingConnection(pika.ConnectionParameters(
        host=cfg.rabbitmq_host(),
        port=cfg.rabbitmq_port(),
        credentials=pika.PlainCredentials(
            cfg.rabbitmq_user(),
            cfg.rabbitmq_pass()
        )
    ))

    channel = conn.channel()
    channel.queue_declare(queue=_SYNC_HTTP_SOURCE_QUEUE)
    channel.basic_consume(_SYNC_HTTP_SOURCE_QUEUE, on_sync_source_message)

    try:
        logger.info('Starting consumer for queue: %s', _SYNC_HTTP_SOURCE_QUEUE)
        channel.start_consuming()
    except KeyboardInterrupt:
        logger.info('Stopping consumer for queue: %s', _SYNC_HTTP_SOURCE_QUEUE)
        channel.stop_consuming()

    conn.close()
