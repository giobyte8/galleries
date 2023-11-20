import os
import synchronizer.config as cfg
from opentelemetry import trace
from synchronizer.db import http_source_dao, http_source_item_dao
from synchronizer.db.models import SIRemoteStatus
from synchronizer.messaging import sync_events_producer
from synchronizer.services import cache_service


tracer = trace.get_tracer_provider().get_tracer(cfg.otel_svc_name())


@tracer.start_as_current_span('sync_http_sources')
def sync_http_sources() -> None:
    """Starts sync process for all http sources
    """
    sources = http_source_dao.all()
    http_source_item_dao.update_status_by_source_ids(
        [source.id for source in sources],
        SIRemoteStatus.UNKNOWN
    )

    sync_events_producer.send_sync_http_source_msgs(sources)


def content_path(source_id: int) -> str:
    cached_path = cache_service.get_source_content_path(source_id)
    if cached_path:
        return cached_path

    source = http_source_dao.find(source_id)
    if not source:
        raise Exception(f'Source with id "{ source_id }" was not found')

    path = os.path.join(cfg.content_root(), source.content_path.strip('/'))
    cache_service.set_source_content_path(source_id, path)

    return path
