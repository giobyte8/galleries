from sqlalchemy import create_engine
from sqlalchemy.orm import Session

import synchronizer.config as cfg

_db_url = 'mysql+pymysql://{user}:{password}@{host}:{port}/{database}'.format(
    user=cfg.mysql_user(),
    password=cfg.mysql_password(),
    host=cfg.mysql_host(),
    port=cfg.mysql_port(),
    database=cfg.mysql_database()
)
engine = create_engine(_db_url, future=True)


def make_session():
    return Session(engine)
