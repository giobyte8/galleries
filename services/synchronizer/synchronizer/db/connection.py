import logging
import synchronizer.config as cfg
from sqlalchemy import create_engine
from sqlalchemy.orm import Session


logger = logging.getLogger(__name__)


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
        _db_session = Session(
            engine,
            expire_on_commit=False # https://stackoverflow.com/a/14899438/3211029
        )

    # TODO Verify that connection stills open
    # From logs:
    #   ConnectionResetError: [Errno 104] Connection reset by peer
    #   sqlalchemy.exc.OperationalError: (pymysql.err.OperationalError) (2006, "MySQL server has gone away (ConnectionResetError(104, 'Connection reset by peer'))")
    #   Background on this error at: https://sqlalche.me/e/14/e3q8
    return _db_session


def close_session():
    if _db_session is not None:
        logger.info('Closing sqlalchemy session')
        _db_session.close()
