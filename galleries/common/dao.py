import contextlib
from pymongo import MongoClient

import galleries.common.config as cfg
from galleries.common.models import FilesGallery, GalleryFile
from galleries.common.schemas import FilesGallerySchema, GalleryFileSchema


files_gl_schema = FilesGallerySchema()
gfile_schema = GalleryFileSchema()


class DBError(Exception):
    def __init__(self, message: str) -> None:
        super().__init__(f'DB Error: { message }')

@contextlib.contextmanager
def _galleries_db():
    try:
        with MongoClient(
            host=cfg.db_host(),
            port=cfg.db_port(),
            username=cfg.db_username(),
            password=cfg.db_password()
        ) as client:
            db = client.galleries
            yield db
    except Exception as e:
        raise DBError(e)


def get_files_galleries() -> list[FilesGallery]:
    with _galleries_db() as db:
        for gallery_doc in db.galleries.find({ 'type': 'files' }):
            gallery = files_gl_schema.load(gallery_doc)
            yield gallery


def save_gallery_file(gfile: GalleryFile):
    with _galleries_db() as db:
        j_gfile = gfile_schema.dump(gfile)
        if not gfile._id:
            j_gfile.pop('_id')

        id = db.gallery_items.insert_one(j_gfile).inserted_id
        gfile._id = id
