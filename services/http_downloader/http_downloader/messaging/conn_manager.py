import contextlib
import pika

import http_downloader.config as cfg
from http_downloader.dl_logging import logger


@contextlib.contextmanager
def rb_channel():
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
