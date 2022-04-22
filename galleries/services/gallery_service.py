from bson.objectid import ObjectId

import galleries.common.cache_dao as cache
from galleries.db import gallery_dao


def content_path(gallery_id: ObjectId) -> str:
    cached_path = cache.gallery_content_path(gallery_id)
    if cached_path:
        return cached_path

    path = gallery_dao.content_path(gallery_id)
    cache.set_gallery_content_path(gallery_id, path)
    return path


def transformations_path(gallery_id: ObjectId) -> str:
    cached_path = cache.gallery_transformations_path(gallery_id)
    if cached_path:
        return cached_path

    path = gallery_dao.transformations_path(gallery_id)
    cache.set_gallery_transformations_path(gallery_id, path)
    return path
