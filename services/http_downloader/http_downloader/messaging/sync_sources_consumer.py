import functools
import json
import pika
import threading
from pika.adapters.blocking_connection import BlockingChannel

import http_downloader.config as cfg
import http_downloader.gdl_wrapper as gdl
from http_downloader.dl_logging import logger
from http_downloader.services.source_service import source_content_abs_path
from http_downloader.messaging import producer


_SYNC_HTTP_SOURCE_QUEUE = 'sync_http_source'
_msg_processor_threads = []


def ack_src_msg(
    channel: BlockingChannel,
    delivery_tag: int,
    src_id: int
) -> None:
    """Notify amqp server that message has been processed successfully so \
        that it can be deleted from queue

    Args:
        channel (BlockingChannel): Pika channel
        delivery_tag (int): Message delivery tag (provided by pika)
        src_id (int): Id of synchronized http source
    """

    # Notify source download completion
    logger.info('Download completed for source: %s', src_id)
    producer.send_source_synchronized(src_id)

    if channel.is_open:
        channel.basic_ack(delivery_tag)
    else:
        logger.warn('Channel closed, message could not be acknowledged')


def process_sync_src_msg(
    channel: BlockingChannel,
    delivery_tag: int,
    body: str
) -> None:
    """Processes given messages (Syncs http source)\
        NOTE: This method should be executed in its own thread
        TODO: Catch possible errors during msg processing and reque or sent to DLQ

    Args:
        channel (BlockingChannel): Pika channcel
        delivery_tag (int): Message delivery tag (Provided by pika)
        body (bytes): Message body
    """
    thread_id = threading.get_ident()
    logger.debug(
        'Thread: %s - Queue: "%s" - Message received: %s',
        thread_id,
        _SYNC_HTTP_SOURCE_QUEUE,
        body)

    # TODO Catch possible parsing errors
    j_body = json.loads(body)
    content_path = source_content_abs_path(j_body['content_path'])

    # Download source contents
    gdl.download(j_body['source_id'], j_body['url'], content_path)

    # Ack message from same thread as connection
    channel.connection.add_callback_threadsafe(functools.partial(
        ack_src_msg,
        channel,
        delivery_tag,
        j_body['source_id']
    ))


def on_sync_source_message(channel: BlockingChannel, basic_deliver, props, body):
    """Callback invoked by pika when a new messages arrives from \
        _SYNC_HTTP_SOURCE queue. \

        A new thread will be created and message will be delegated to \
        'process_sync_src_message' inside such thread.

    Args:
        channel (BlockingChannel): pika channel
        basic_deliver (_type_):
        props ():
        body (str): Raw message from queue
    """
    t = threading.Thread(
        target=process_sync_src_msg,
        args=(channel, basic_deliver.delivery_tag, body.decode('utf-8')))
    t.start()
    _msg_processor_threads.append(t)


def start_consumer():
    conn = pika.BlockingConnection(pika.ConnectionParameters(
        host=cfg.rabbitmq_host(),
        port=cfg.rabbitmq_port(),
        credentials=pika.PlainCredentials(
            cfg.rabbitmq_user(),
            cfg.rabbitmq_pass()
        ),
        heartbeat=10
    ))

    channel = conn.channel()

    # Fetch two messages at a time and don't receive more until
    # previous messages are aknowledged
    channel.basic_qos(prefetch_count=2)

    channel.queue_declare(queue=_SYNC_HTTP_SOURCE_QUEUE)
    channel.basic_consume(_SYNC_HTTP_SOURCE_QUEUE, on_sync_source_message)

    try:
        logger.info('Starting consumer for queue: %s', _SYNC_HTTP_SOURCE_QUEUE)
        channel.start_consuming()
    except KeyboardInterrupt:
        logger.info('Stopping consumer for queue: %s', _SYNC_HTTP_SOURCE_QUEUE)
        channel.stop_consuming()

    for t in _msg_processor_threads:
        t.join()

    conn.close()
