import os
from bson.objectid import ObjectId

import galleries.common.cache_dao as cache
import galleries.common.futils as futils
import galleries.services.gallery_service as gl_service
from galleries.db import gallery_file_dao
from galleries.common.models import RemoteStatus


def set_download_skipped_items_as_found(gallery_id: ObjectId) -> None:
    """Gets all files skipped from download in cache for given gallery \
        and mark them as 'FOUND'

    Args:
        gallery_id (ObjectId): Gallery owning files to process
    """
    for skipped_item in cache.skipped_items(gallery_id):
        source_id = ObjectId(skipped_item['source_id'])
        filename = skipped_item['filename']
        gallery_file_dao.set_remote_status(
            gallery_id,
            source_id,
            filename,
            RemoteStatus.FOUND
        )


def delete_remote_unknown_files(gallery_id: ObjectId) -> None:
    """All files of given gallery with remote status equals to 'UNKNOWN'  \
        will be removed (Physical and from database) along with its       \
        transformed versiones

    Args:
        gallery_id (ObjectId): Gallery owning files to remove
    """
    content_path = gl_service.content_path(gallery_id)
    trans_path = gl_service.transformations_path(gallery_id)

    gl_files_to_delete_count = gallery_file_dao.count_by_gallery_and_remote_status(
        gallery_id,
        RemoteStatus.UNKNOWN
    )
    while gl_files_to_delete_count > 0:
        ids_to_delete: list[ObjectId] = []

        for gl_file in gallery_file_dao.paginate_by_gallery_and_remote_status(
            gallery_id,
            RemoteStatus.UNKNOWN,
            page_size=100,
            page_index=0
        ):
            for trans_version in gl_file.transformed_versions:
                f_path = os.path.join(trans_path, trans_version.rel_file_path)
                if not futils.remove(f_path):
                    print(f'File could not be removed: { f_path }')

            f_path = os.path.join(content_path, gl_file.filename)
            if not futils.remove(f_path):
                print(f'File could not be removed: { f_path }')

            ids_to_delete.append(gl_file._id)

        gallery_file_dao.delete_by_ids(ids_to_delete)
        gl_files_to_delete_count = gallery_file_dao\
            .count_by_gallery_and_remote_status(
                gallery_id,
                RemoteStatus.UNKNOWN
            )
