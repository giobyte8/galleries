import logging
import os
from synchronizer import futils
from synchronizer.services import http_source_service as h_src_svc


logger = logging.getLogger(__name__)


def abs_path(source_id: int, filename: str) -> str:
    return os.path.join(
        h_src_svc.content_path(source_id),
        filename.strip('/')
    )


def remove(source_id: int, filename: str) -> None:
    filepath = abs_path(source_id, filename)

    if not futils.remove(filepath):
        logger.warn('File could not be removed: %s', filepath)
