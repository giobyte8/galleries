import logging
from opentelemetry import trace
from typing import List
from sqlalchemy import delete, select, update
from synchronizer import config as cfg
from synchronizer.db.connection import get_session
from synchronizer.db.models import HttpSourceItem, SIRemoteStatus


logger = logging.getLogger(__name__)
tracer = trace.get_tracer(cfg.otel_svc_name())


def find_by_src_and_status(
    source_id: int,
    status: SIRemoteStatus
) -> List[HttpSourceItem]:
    stmt = select(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.remote_status == status.value)

    return get_session().scalars(stmt).all()


def upsert(source_id: int, filename: str) -> HttpSourceItem:
    stmt = select(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.filename == filename)

    session = get_session()
    item = session.scalars(stmt).first()

    if not item:
        item = HttpSourceItem(
            source_id=source_id,
            filename=filename,
            remote_status=SIRemoteStatus.FOUND.value
        )

        session.add(item)
        session.commit()


def update_remote_status(
    source_id: int,
    filename: str,
    status: SIRemoteStatus
) -> None:
    stmt = update(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.filename == filename)\
        .values(remote_status=status.value)

    session = get_session()
    session.execute(stmt)
    session.commit()


def update_status_by_src_and_status(
    source_id: int,
    from_status: SIRemoteStatus,
    to_status: SIRemoteStatus
) -> None:
    if from_status == to_status:
        logger.warn(
            ('Source status "%s" is the same as target status,'
             ' skipping update operation'),
            from_status.value
        )
        return

    stmt = update(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.remote_status == from_status.value)\
        .values(remote_status=to_status.value)

    session = get_session()
    session.execute(stmt)
    session.commit()


@tracer.start_as_current_span("http_source_item_dao.update_status_by_source_ids")
def update_status_by_source_ids(
    source_ids: List[int],
    status: SIRemoteStatus
) -> None:
    stmt = update(HttpSourceItem)\
        .where(HttpSourceItem.source_id.in_(source_ids))\
        .values(remote_status=status.value)

    session = get_session()
    session.execute(stmt)
    session.commit()


def delete_by_src_and_status(
    source_id: int,
    status: SIRemoteStatus
) -> None:
    stmt = delete(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.remote_status == status.value)

    session = get_session()
    session.execute(stmt)
    session.commit()
