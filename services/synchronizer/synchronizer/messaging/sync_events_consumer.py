import json
import logging
from kombu import Consumer, eventloop
from kombu.utils.compat import nested

from synchronizer.db.connection import close_session
from synchronizer.messaging.resources import (
    FILE_DL_QUEUE,
    FILE_SK_QUEUE,
    SRC_SYNCHRONIZED_QUEUE,
    rb_connection
)
from synchronizer.services import sync_service


logger = logging.getLogger(__name__)


def on_file_skipped(body, message):
    j_body = json.loads(body)
    logger.debug('File skipped msg received: %s', j_body["filename"])

    source_id = int(j_body['source_id'])
    filename = j_body['filename']
    sync_service.on_file_skipped(source_id, filename)
    message.ack()


def on_file_downloaded(body, message):
    j_body = json.loads(body)
    logger.debug('File downloaded msg received: %s', j_body["filename"])

    source_id = int(j_body['source_id'])
    filename = j_body['filename']
    sync_service.on_file_downloaded(source_id, filename)
    message.ack()


def on_source_synchronized(body, message):
    j_body = json.loads(body)
    logger.debug('Source synchronized msg received: %s', j_body["source_id"])

    source_id = int(j_body['source_id'])
    sync_service.on_source_downloaded(source_id)
    message.ack()


def start_consumer():
    with rb_connection() as conn:
        file_skipped_cmr = Consumer(
            conn,
            FILE_SK_QUEUE,
            callbacks=[on_file_skipped]
        )
        file_downloaded_cmr = Consumer(
            conn,
            FILE_DL_QUEUE,
            callbacks=[on_file_downloaded]
        )
        source_synchronized_cmr = Consumer(
            conn,
            SRC_SYNCHRONIZED_QUEUE,
            callbacks=[on_source_synchronized]
        )

        logger.info('Starting sync events consumption')
        with nested(
            file_skipped_cmr,
            file_downloaded_cmr,
            source_synchronized_cmr
        ):
            try:
                for _ in eventloop(conn):
                    pass
            except KeyboardInterrupt:
                close_session()
                logger.info('Stopping sync events consumption')
