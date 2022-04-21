import typing
from bson.objectid import ObjectId

from galleries.db.db_connection import galleries_db
from galleries.common.models import GalleryFile, RemoteStatus
from galleries.common.schemas import GalleryFileSchema


_gl_file_schema = GalleryFileSchema()


def paginate_by_gallery_and_delete_on_remote(
    gallery_id: ObjectId,
    status: RemoteStatus,
    page_size: int,
    page_index: int
) -> typing.Generator[GalleryFile, None, None]:
    pass

def save(gl_file: GalleryFile) -> None:
    with galleries_db() as db:
        j_gl_file = _gl_file_schema.dump(gl_file)
        if not gl_file._id:
            j_gl_file.pop('_id')

        gl_file._id = db.gallery_items.insert_one(j_gl_file).inserted_id


def set_deleted_on_remote(
    gallery_id: ObjectId,
    source_id: ObjectId,
    filename: str,
    status: RemoteStatus
) -> int:
    with galleries_db() as db:
        return db.gallery_items\
            .update_one(
                {
                    'gallery_id': gallery_id,
                    'source_id': source_id,
                    'filename': filename
                },
                { '$set': { 'deleted_on_remote': status.value }}
            )\
            .modified_count


def set_deleted_on_remote_by_gallery(
    gallery_id: ObjectId,
    status: RemoteStatus
) -> int:
    with galleries_db() as db:
        return db.gallery_items\
            .update_many(
                { 'gallery_id': gallery_id },
                { '$set': { 'deleted_on_remote': status.value }}
            )\
            .modified_count
