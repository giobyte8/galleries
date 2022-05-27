import redis
from typing import Union
import synchronizer.config as cfg


_SOURCES_CONTENT_KEY = 'source_content_path'

r = redis.Redis(cfg.redis_host(), cfg.redis_port())


def set_source_content_path(source_id: int, path: str):
    r.hset(_SOURCES_CONTENT_KEY, source_id, path)


def get_source_content_path(source_id: int) -> Union[str, None]:
    path = r.hget(_SOURCES_CONTENT_KEY, source_id)
    if path:
        return path.decode('utf-8')
    return None
