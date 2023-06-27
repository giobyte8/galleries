from kombu import Connection, Exchange, Queue
import synchronizer.config as cfg


class QueueNames:
    FILE_DOWNLOADED = cfg.amqp_q_file_downloaded()
    FILE_SKIPPED = cfg.amqp_q_file_download_skipped()
    SYNC_HTTP_SOURCE = cfg.amqp_q_sync_http_src_orders()
    SOURCE_SYNCHRONIZED = cfg.amqp_q_source_synchronized()

# Declare exchange ('direct' type and durable are default values)
gl_exchange = Exchange(cfg.amqp_exchange())


SYNC_HTTP_SRC_QUEUE = Queue(
    QueueNames.SYNC_HTTP_SOURCE,
    exchange=gl_exchange,
    routing_key=QueueNames.SYNC_HTTP_SOURCE,
    durable=False
)

FILE_SK_QUEUE = Queue(
    QueueNames.FILE_SKIPPED,
    exchange=gl_exchange,
    routing_key=QueueNames.FILE_SKIPPED,
    durable=False
)

FILE_DL_QUEUE = Queue(
    QueueNames.FILE_DOWNLOADED,
    exchange=gl_exchange,
    routing_key=QueueNames.FILE_DOWNLOADED,
    durable=False
)

SRC_SYNCHRONIZED_QUEUE = Queue(
    QueueNames.SOURCE_SYNCHRONIZED,
    exchange=gl_exchange,
    routing_key=QueueNames.SOURCE_SYNCHRONIZED,
    durable=False
)


def rb_connection() -> Connection:
    """Gets a rabbitmq connection from pool

    Returns:
        Connection: Connection to rabbitmq
    """
    return Connection(
        hostname=cfg.rabbitmq_host(),
        port=cfg.rabbitmq_port(),
        userid=cfg.rabbitmq_user(),
        password=cfg.rabbitmq_pass()
    )
