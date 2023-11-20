import logging
from opentelemetry import trace
from typing import List
from synchronizer import config as cfg
from synchronizer.db.models import HttpSource
from synchronizer.messaging.resources import (
    QueueNames,
    SYNC_HTTP_SRC_QUEUE,
    rb_connection
)


logger = logging.getLogger(__name__)
tracer = trace.get_tracer_provider().get_tracer(cfg.otel_svc_name())


@tracer.start_as_current_span('amqp_sync_sources')
def send_sync_http_source_msgs(sources: List[HttpSource]) -> None:
    """Publishes messages to request synchronization of each given source

    Args:
        sources (List[HttpSource]): Queues to synchronize, a message \
            will be published for each provided queue
    """
    with rb_connection() as conn:
        producer = conn.Producer()

        for src in sources:
            msg = {
                'source_id': src.id,
                'url': src.url,
                'content_path': src.content_path
            }

            with tracer.start_as_current_span('amqp_publish') as sp:
                sp.set_attributes({ 'src.source_id': src.id, 'src.url': src.url })

                producer.publish(
                    msg,
                    routing_key=QueueNames.SYNC_HTTP_SOURCE,
                    declare=[SYNC_HTTP_SRC_QUEUE]
                )
                logger.info('Source queued for sync: %s', src.id)
