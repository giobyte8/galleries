from synchronizer.db import (
    http_source_dao as h_src_dao,
    http_source_item_dao as h_src_item_dao,
)
from synchronizer.db.models import SIRemoteStatus
from synchronizer.services import (
    source_file_service as src_file_svc
)


def on_file_downloaded(source_id: int, filename: str) -> None:
    h_src_item_dao.upsert(source_id, filename)


def on_file_skipped(source_id: int, filename: str) -> None:
    h_src_item_dao.update_remote_status(
        source_id,
        filename,
        SIRemoteStatus.FOUND
    )


def on_source_downloaded(source_id: int) -> None:
    src = h_src_dao.find(source_id)

    if src.sync_remote_deletes:
        items = h_src_item_dao.find_by_src_and_status(
            source_id,
            SIRemoteStatus.UNKNOWN
        )
        for item in items:
            src_file_svc.remove(item.source_id, item.filename)

        h_src_item_dao.delete_by_src_and_status(
            source_id,
            SIRemoteStatus.UNKNOWN
        )
    else:
        h_src_item_dao.update_status_by_src_and_status(
            source_id,
            SIRemoteStatus.UNKNOWN,
            SIRemoteStatus.NOT_FOUND
        )
