import json

from http_downloader.dl_logging import logger
from http_downloader.messaging.conn_manager import rb_channel


_FILE_DOWNLOADED_QUEUE = 'file_downloaded'
_FILE_SKIPPED_QUEUE = 'file_skipped'
_SOURCE_SYNCHRONIZED_QUEUE = 'source_synchronized'


def send_file_downloaded(source_id: str, filename: str) -> None:
    """Publishes a message to inform about a new downloaded file \
        for given source

    Args:
        source_id (str): Source from where file was downloaded
        filename (str): Downloaded file name
    """
    with rb_channel() as channel:
        channel.queue_declare(_FILE_DOWNLOADED_QUEUE)

        msg = {
            'source_id': source_id,
            'filename': filename
        }

        channel.basic_publish(
            exchange='',
            routing_key=_FILE_DOWNLOADED_QUEUE,
            body=json.dumps(msg)
        )

        logger.info(
            'Downloaded file message sent for: %s and source: %s',
            filename,
            source_id)


def send_file_skipped(source_id: str, filename: str) -> None:
    """Publishes a message to inform about a skipped file \
        for given source

    Args:
        source_id (str): Source from where file was skipped
        filename (str): Skipped file name
    """
    with rb_channel() as channel:
        channel.queue_declare(_FILE_SKIPPED_QUEUE)

        msg = {
            'source_id': source_id,
            'filename': filename
        }

        channel.basic_publish(
            exchange='',
            routing_key=_FILE_SKIPPED_QUEUE,
            body=json.dumps(msg)
        )

        logger.info(
            'Skipped file message sent for: %s and source: %s',
            filename,
            source_id)


def send_source_synchronized(source_id: str) -> None:
    """Publishes a message to inform that given source has been \
        synchronized

    Args:
        source_id (str): Id of synchronized source
    """
    with rb_channel() as channel:
        channel.queue_declare(_SOURCE_SYNCHRONIZED_QUEUE)

        msg = { 'source_id': source_id }
        channel.basic_publish(
            exchange='',
            routing_key=_SOURCE_SYNCHRONIZED_QUEUE,
            body=json.dumps(msg)
        )

        logger.info(
            'Source synchronized message sent for source: %s',
            source_id
        )
