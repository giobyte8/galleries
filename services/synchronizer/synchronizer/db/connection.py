from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from synchronizer.sync_logging import logger

import synchronizer.config as cfg

_db_url = 'mysql+pymysql://{user}:{password}@{host}:{port}/{database}'.format(
    user=cfg.mysql_user(),
    password=cfg.mysql_password(),
    host=cfg.mysql_host(),
    port=cfg.mysql_port(),
    database=cfg.mysql_database()
)
engine = create_engine(_db_url, future=True)

_db_session: Session = None


def get_session() -> Session:
    global _db_session

    if not _db_session:
        logger.info('Creating sqlalchemy session')
        _db_session = Session(engine)

    return _db_session


def close_session():
    if _db_session is not None:
        logger.info('Closing sqlalchemy session')
        _db_session.close()
