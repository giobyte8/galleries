from typing import List
from sqlalchemy import delete, select, update
from synchronizer.db.connection import make_session
from synchronizer.db.models import HttpSourceItem, SIRemoteStatus
from synchronizer.sync_logging import logger


def find_by_src_and_status(
    source_id: int,
    status: SIRemoteStatus
) -> List[HttpSourceItem]:
    stmt = select(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.remote_status == status.value)

    with make_session() as session:
        return session.scalars(stmt).all()


def upsert(source_id: int, filename: str) -> HttpSourceItem:
    stmt = select(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.filename == filename)

    with make_session() as session:
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

    with make_session() as session:
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
    with make_session() as session:
        session.execute(stmt)
        session.commit()


def delete_by_src_and_status(
    source_id: int,
    status: SIRemoteStatus
) -> None:
    stmt = delete(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .where(HttpSourceItem.remote_status == status.value)

    with make_session() as session:
        session.execute(stmt)
        session.commit()
