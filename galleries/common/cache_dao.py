import json
import redis
import typing
import galleries.common.config as cfg
from bson.objectid import ObjectId
from typing import Union


SKIPPED_PREFIX='skipped-'
GL_CONTENT_PATH_PREFIX='gl-content-path-'
GL_TRANSFORMATIONS_PATH_PREFIX='gl-transformations-path-'

r = redis.Redis(cfg.cache_host(), cfg.cache_port())


def add_skipped_item(
    gallery_id: str,
    source_id: str,
    filename: str
) -> None:
    key = f'{ SKIPPED_PREFIX }{ gallery_id }'
    item = {
        'gallery_id': gallery_id,
        'source_id': source_id,
        'filename': filename
    }

    r.sadd(key, json.dumps(item))


def skipped_items(gallery_id: ObjectId) -> typing.Generator[dict, None, None]:
    key = f'{ SKIPPED_PREFIX }{ str(gallery_id) }'
    while r.scard(key) > 0:
        yield json.loads(r.spop(key))


def gallery_content_path(gallery_id: ObjectId) -> Union[str, None]:
    key = f'{ GL_CONTENT_PATH_PREFIX }{ str(gallery_id) }'
    path = r.get(key)
    if path:
        return path.decode('utf-8')
    return None


def set_gallery_content_path(gallery_id: ObjectId, path: str) -> None:
    key = f'{ GL_CONTENT_PATH_PREFIX }{ str(gallery_id) }'
    r.set(key, path)


def gallery_transformations_path(gallery_id: ObjectId) -> Union[str, None]:
    key = f'{ GL_TRANSFORMATIONS_PATH_PREFIX }{ str(gallery_id) }'
    path = r.get(key)
    if path:
        return path.decode('utf-8')
    return None


def set_gallery_transformations_path(gallery_id: ObjectId, path: str) -> None:
    key = f'{ GL_TRANSFORMATIONS_PATH_PREFIX }{ str(gallery_id) }'
    r.set(key, path)
