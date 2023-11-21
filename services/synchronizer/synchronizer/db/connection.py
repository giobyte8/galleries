import logging
import synchronizer.config as cfg
from sqlalchemy import create_engine
from sqlalchemy.exc import OperationalError
from sqlalchemy.orm import Session
from tenacity import (
    before_sleep_log,
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_random
)
from typing import Any, Union


logger = logging.getLogger(__name__)

__db_session: Session = None
_db_url = 'mysql+pymysql://{user}:{password}@{host}:{port}/{database}'.format(
    user=cfg.mysql_user(),
    password=cfg.mysql_password(),
    host=cfg.mysql_host(),
    port=cfg.mysql_port(),
    database=cfg.mysql_database()
)
engine = create_engine(_db_url, future=True)


def __get_session() -> Session:
    global __db_session
    if not __db_session:
        logger.info('Creating sqlalchemy session')
        __db_session = Session(
            engine,
            expire_on_commit=False # https://stackoverflow.com/a/14899438/3211029
        )
    return __db_session


# Retry was added to address scenarios where MySQL drops the connection
# From error logs:
#   ConnectionResetError: [Errno 104] Connection reset by peer
#   sqlalchemy.exc.OperationalError: (pymysql.err.OperationalError)   \
#       (2006, "MySQL server has gone away (ConnectionResetError(104, \
#       'Connection reset by peer'))")
#   Background on this error at: https://sqlalche.me/e/14/e3q8
@retry(
    wait=wait_random(min=1, max=3),
    stop=stop_after_attempt(3),
    retry=retry_if_exception_type(OperationalError),
    before_sleep=before_sleep_log(logger, logging.WARNING),
)
def db_add(record, commit: bool = True) -> None:
    __get_session().add(record)

    if commit:
        __get_session().commit()


@retry(
    wait=wait_random(min=1, max=3),
    stop=stop_after_attempt(3),
    retry=retry_if_exception_type(OperationalError),
    before_sleep=before_sleep_log(logger, logging.WARNING),
)
def db_exec(stmt, commit: bool = True) -> None:
    __get_session().execute(stmt)

    if commit:
        __get_session().commit()


@retry(
    wait=wait_random(min=1, max=3),
    stop=stop_after_attempt(3),
    retry=retry_if_exception_type(OperationalError),
    before_sleep=before_sleep_log(logger, logging.WARNING),
)
def find_all(stmt) -> list:
    return __get_session().scalars(stmt).all()


@retry(
    wait=wait_random(min=1, max=3),
    stop=stop_after_attempt(3),
    retry=retry_if_exception_type(OperationalError),
    before_sleep=before_sleep_log(logger, logging.WARNING),
)
def find_first(stmt) -> Union[Any, None]:
    return __get_session().scalars(stmt).first()


def cleanup():
    global __db_session

    if __db_session is not None:
        logger.info('Closing sqlalchemy session')
        __db_session.close()
