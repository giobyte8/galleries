import json
import redis
import typing
import galleries.common.config as cfg


SKIPPED_PREFIX='skipped-'

r = redis.Redis(cfg.cache_host(), cfg.cache_port())


def add_skipped_file(
    gallery_id: str,
    source_id: str,
    filename: str
) -> None:
    key = f'skipped-{ gallery_id }'
    item = {
        'gallery_id': gallery_id,
        'source_id': source_id,
        'filename': filename
    }

    r.sadd(key, json.dumps(item))


def skipped_items(gallery_id: str) -> typing.Generator[dict, None, None]:
    key = f'skipped-{ gallery_id }'
    while r.scard(key) > 0:
        yield json.loads(r.spop(key))
