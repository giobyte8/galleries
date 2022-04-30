import contextlib
from pymongo import MongoClient

import galleries.common.config as cfg


class DBError(Exception):
    def __init__(self, message: str) -> None:
        super().__init__(f'DB Error: { message }')

@contextlib.contextmanager
def galleries_db():
    try:
        with MongoClient(
            host=cfg.mongodb_host(),
            port=cfg.mongodb_port(),
            username=cfg.mongodb_username(),
            password=cfg.mongodb_password()
        ) as client:
            db = client.galleries
            yield db
    except Exception as e:
        raise DBError(e)
