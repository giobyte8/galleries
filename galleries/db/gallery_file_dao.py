import typing
from bson.objectid import ObjectId

from galleries.db.db_connection import galleries_db
from galleries.common.models import GalleryFile, RemoteStatus
from galleries.common.schemas import GalleryFileSchema


_gl_file_schema = GalleryFileSchema()


def count_by_gallery_and_remote_status(
    gallery_id: ObjectId,
    status: RemoteStatus
) -> int:
    with galleries_db() as db:
        return db.gallery_items.count_documents({
            'gallery_id': gallery_id,
            'remote_status': status.value
        })


def paginate_by_gallery_and_remote_status(
    gallery_id: ObjectId,
    status: RemoteStatus,
    page_size: int,
    page_index: int
) -> typing.Generator[GalleryFile, None, None]:
    with galleries_db() as db:
        skips = page_size * page_index
        cursor = db.gallery_items\
            .find(
                {
                    'gallery_id': gallery_id,
                    'remote_status': status.value
                }
            )\
            .skip(skips)\
            .limit(page_size)

        for gl_file_doc in cursor:
            gl_file = _gl_file_schema.load(gl_file_doc)
            yield gl_file


def save(gl_file: GalleryFile) -> None:
    with galleries_db() as db:
        j_gl_file = _gl_file_schema.dump(gl_file)
        if not gl_file._id:
            j_gl_file.pop('_id')

        gl_file._id = db.gallery_items.insert_one(j_gl_file).inserted_id


def set_remote_status(
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
                { '$set': { 'remote_status': status.value }}
            )\
            .modified_count


def set_remote_status_by_gallery(
    gallery_id: ObjectId,
    status: RemoteStatus
) -> int:
    with galleries_db() as db:
        return db.gallery_items\
            .update_many(
                { 'gallery_id': gallery_id },
                { '$set': { 'remote_status': status.value }}
            )\
            .modified_count


def delete_by_ids(gl_files_ids: list[ObjectId]) -> int:
    with galleries_db() as db:
        return db.gallery_items\
            .delete_many({ '_id': {'$in': gl_files_ids }})\
            .deleted_count
