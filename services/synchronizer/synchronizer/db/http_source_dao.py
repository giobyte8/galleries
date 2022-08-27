from typing import List
from sqlalchemy import select, update
from synchronizer.db.connection import get_session
from synchronizer.db.models import HttpSource, HttpSourceItem, SIRemoteStatus


def all() -> List[HttpSource]:
    stmt = select(HttpSource)
    return get_session().scalars(stmt).all()


def find(id: int) -> HttpSource:
    stmt = select(HttpSource).where(HttpSource.id == id)
    return get_session().scalars(stmt).first()


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
