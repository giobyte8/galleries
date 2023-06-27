from typing import List
from synchronizer.db.models import HttpSource
from synchronizer.sync_logging import logger
from synchronizer.messaging.resources import (
    QueueNames,
    SYNC_HTTP_SRC_QUEUE,
    rb_connection
)


def send_sync_http_source_msgs(sources: List[HttpSource]) -> None:
    """Publishes messages to request synchronization of each given source

    Args:
        sources (List[HttpSource]): Queues to synchronize, a message \
            will be published for each provided queue
    """
    with rb_connection() as conn:
        producer = conn.Producer()

        for source in sources:
            msg = {
                'source_id': source.id,
                'url': source.url,
                'content_path': source.content_path
            }

            producer.publish(
                msg,
                routing_key=QueueNames.SYNC_HTTP_SOURCE,
                declare=[SYNC_HTTP_SRC_QUEUE]
            )
            logger.info('Source queued for sync: %s', source.id)
