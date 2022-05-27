from kombu import Connection, Queue
import synchronizer.config as cfg


class QueueNames:
    FILE_DOWNLOADED = 'file_downloaded'
    FILE_SKIPPED = 'file_skipped'
    SYNC_HTTP_SOURCE = 'sync_http_source'
    SOURCE_SYNCHRONIZED = 'source_synchronized'


SYNC_HTTP_SRC_QUEUE = Queue(
    QueueNames.SYNC_HTTP_SOURCE,
    routing_key=QueueNames.SYNC_HTTP_SOURCE,
    durable=False
)

FILE_SK_QUEUE = Queue(
    QueueNames.FILE_SKIPPED,
    routing_key=QueueNames.FILE_SKIPPED,
    durable=False
)

FILE_DL_QUEUE = Queue(
    QueueNames.FILE_DOWNLOADED,
    routing_key=QueueNames.FILE_DOWNLOADED,
    durable=False
)

SRC_SYNCHRONIZED_QUEUE = Queue(
    QueueNames.SOURCE_SYNCHRONIZED,
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
