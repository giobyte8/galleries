from typing import List
from sqlalchemy import select, update
from synchronizer.db.connection import make_session
from synchronizer.db.models import HttpSource, HttpSourceItem, SIRemoteStatus


def all() -> List[HttpSource]:
    stmt = select(HttpSource)
    with make_session() as session:
        return session.scalars(stmt).all()


def find(id: int) -> HttpSource:
    stmt = select(HttpSource).where(HttpSource.id == id)
    with make_session() as session:
        return session.scalars(stmt).first()


def update_items_remote_status(
    source_id: int,
    status: SIRemoteStatus
) -> None:
    stmt = update(HttpSourceItem)\
        .where(HttpSourceItem.source_id == source_id)\
        .values(remote_status=status.value)

    with make_session() as session:
        session.execute(stmt)
        session.commit()
