import os
import synchronizer.config as cfg
from synchronizer.db import http_source_dao
from synchronizer.db.models import SIRemoteStatus
from synchronizer.messaging import sync_events_producer
from synchronizer.services import cache_service


def sync_http_sources() -> None:
    """Starts sync process for all http sources
    """
    sources = http_source_dao.all()
    for source in sources:
        http_source_dao.update_items_remote_status(
            source.id,
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
