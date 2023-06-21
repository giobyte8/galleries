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
            ),
            heartbeat=10
        ))

        yield conn.channel()
        conn.close()
    except Exception as e:
        logger.error('RabbitMQ connection failed: %s', e)


def init_amqp_resources():
    """Verifies that AMQP exchange, queues and bindings are declared in
    broker with appropiate attributes.

    Make sure to invoke this method once before start service operation
    """
    with rb_channel() as ch:
        try:
            logger.debug(
                'Verifying AMQP exchange, queues and bindings are declared'
            )

            ch.exchange_declare(cfg.amqp_exchange(), durable=True)

            # Queue to receive sync http source requests
            ch.queue_declare(cfg.amqp_q_sync_http_src_orders(), durable=False)
            ch.queue_bind(
                queue=cfg.amqp_q_sync_http_src_orders(),
                exchange=cfg.amqp_exchange(),
                routing_key=cfg.amqp_q_sync_http_src_orders()
            )

            ch.queue_declare(cfg.amqp_q_file_downloaded())
            ch.queue_bind(
                queue=cfg.amqp_q_file_downloaded(),
                exchange=cfg.amqp_exchange(),
                routing_key=cfg.amqp_q_file_downloaded()
            )

            ch.queue_declare(cfg.amqp_q_file_download_skipped())
            ch.queue_bind(
                queue=cfg.amqp_q_file_download_skipped(),
                exchange=cfg.amqp_exchange(),
                routing_key=cfg.amqp_q_file_download_skipped()
            )

            ch.queue_declare(cfg.amqp_q_source_synchronized())
            ch.queue_bind(
                queue=cfg.amqp_q_source_synchronized(),
                exchange=cfg.amqp_exchange(),
                routing_key=cfg.amqp_q_source_synchronized()
            )
        except Exception as e:
            logger.error('RabbitMQ resources init failed: %s', e)
