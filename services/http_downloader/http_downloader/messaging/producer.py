import json
import http_downloader.config as cfg

from http_downloader.dl_logging import logger
from http_downloader.messaging.conn_manager import rb_channel


def send_file_downloaded(source_id: int, filename: str) -> None:
    """Publishes a message to inform about a new downloaded file \
        for given source

    Args:
        source_id (str): Source from where file was downloaded
        filename (str): Downloaded file name
    """
    with rb_channel() as channel:
        msg = {
            'source_id': source_id,
            'filename': filename
        }

        channel.basic_publish(
            exchange=cfg.amqp_exchange(),
            routing_key=cfg.amqp_q_file_downloaded(),
            body=json.dumps(msg)
        )

        logger.info(
            '"FILE_DOWNLOADED" amqp msg sent for: %s and source: %s',
            filename,
            source_id
        )


def send_file_skipped(source_id: int, filename: str) -> None:
    """Publishes a message to inform about a skipped file \
        for given source

    Args:
        source_id (str): Source from where file was skipped
        filename (str): Skipped file name
    """
    with rb_channel() as channel:
        msg = {
            'source_id': source_id,
            'filename': filename
        }

        channel.basic_publish(
            exchange=cfg.amqp_exchange(),
            routing_key=cfg.amqp_q_file_download_skipped(),
            body=json.dumps(msg)
        )

        logger.info(
            '"FILE_DOWNLOAD_SKIPPED" amqp msg sent for: %s and source: %s',
            filename,
            source_id
        )


def send_source_synchronized(source_id: int) -> None:
    """Publishes a message to inform that given source has been \
        synchronized

    Args:
        source_id (int): Id of synchronized source
    """
    with rb_channel() as channel:
        msg = { 'source_id': source_id }

        channel.basic_publish(
            exchange=cfg.amqp_exchange(),
            routing_key=cfg.amqp_q_source_synchronized(),
            body=json.dumps(msg)
        )

        logger.info(
            '"SOURCE_SYNCHRONIZED" amqp msg sent for source: %s',
            source_id
        )
