import contextlib
from bson.objectid import ObjectId
from pymongo import MongoClient

import galleries.common.config as cfg
from galleries.common.models import (
    FilesGallery,
    GalleryFile,
    RemoteStatus
)
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


def set_files_remote_status(
    gallery_id: ObjectId,
    status: RemoteStatus
) -> int:
    with _galleries_db() as db:
        db.gallery_items\
            .update_many(
                { 'gallery_id': gallery_id },
                { '$set': { 'deleted_on_remote': str(status) }}
            )\
            .modified_count
