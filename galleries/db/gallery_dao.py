import typing
from bson.objectid import ObjectId

from galleries.common.models import FilesGallery
from galleries.common.schemas import FilesGallerySchema
from galleries.db import db_connection


files_gl_schema = FilesGallerySchema()


def all() -> typing.Generator[FilesGallery, None, None]:
    with db_connection.galleries_db() as db:
        for gallery_doc in db.galleries.find({ 'type': 'files' }):
            gallery = files_gl_schema.load(gallery_doc)
            yield gallery


def save(files_gl: FilesGallery) -> None:
    with db_connection.galleries_db()  as db:
        j_files_gl = files_gl_schema.dump(files_gl)
        if not files_gl._id:
            j_files_gl.pop('_id')

        files_gl._id = db.galleries.insert_one(j_files_gl).inserted_id


def content_path(gallery_id: ObjectId) -> str:
    with db_connection.galleries_db() as db:
        gl_doc = db.galleries.find_one({ '_id': gallery_id })
        gallery = files_gl_schema.load(gl_doc)
        return gallery.abs_path()


def transformations_path(gallery_id: ObjectId) -> str:
    with db_connection.galleries_db() as db:
        gl_doc = db.galleries.find_one({ '_id': gallery_id })
        gallery = files_gl_schema.load(gl_doc)
        return gallery.abs_trans_path()
