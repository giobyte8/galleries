import contextlib
from pymongo import MongoClient

import galleries.common.config as cfg
from galleries.common.models import FilesGallery
from galleries.common.schemas import FilesGallerySchema


class DBError(Exception):
    def __init__(self, message: str) -> None:
        super().__init__(f'DB Error: { message }')


@contextlib.contextmanager
def _galleries_collection():
    try:
        with MongoClient(
            host=cfg.db_host(),
            port=cfg.db_port(),
            username=cfg.db_username(),
            password=cfg.db_password()
        ) as client:
            db = client.galleries
            yield db.galleries
    except Exception as e:
        raise DBError(e)

def get_files_galleries() -> list[FilesGallery]:
    with _galleries_collection() as galleries:
        files_gl_schema = FilesGallerySchema()
        for gallery_doc in galleries.find({ 'type': 'files' }):
            gallery = files_gl_schema.load(gallery_doc)
            yield gallery
