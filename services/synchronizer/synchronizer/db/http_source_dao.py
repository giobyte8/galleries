from opentelemetry import trace
from typing import List
from sqlalchemy import select, update
from synchronizer import config as cfg
from synchronizer.db.connection import get_session
from synchronizer.db.models import HttpSource, HttpSourceItem, SIRemoteStatus


tracer = trace.get_tracer(cfg.otel_svc_name())


@tracer.start_as_current_span("http_source_dao.all")
def all() -> List[HttpSource]:
    stmt = select(HttpSource)
    return get_session().scalars(stmt).all()


@tracer.start_as_current_span("http_source_dao.find")
def find(id: int) -> HttpSource:
    stmt = select(HttpSource).where(HttpSource.id == id)
    return get_session().scalars(stmt).first()


@tracer.start_as_current_span("http_source_dao.update_items_remote_status")
def update_items_remote_status(
    source_id: int,
    status: SIRemoteStatus
) -> None:
    stmt = update(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .values(remote_status=status.value)

    session = get_session()
    session.execute(stmt)
    session.commit()
