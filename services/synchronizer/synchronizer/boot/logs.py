import logging
import synchronizer.config as cfg
import os
from logging.handlers import RotatingFileHandler


_LOGGER_NAME = 'synchronizer'
_LOG_FILENAME = f'{_LOGGER_NAME}.log'
_FORMATTER = logging.Formatter(
    '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)


def parse_log_level(level: str):
    """Parses a given string value into a valid python
    log level number. Defaults to 'INFO'

    Args:
        level (str): One of 'CRITICAL', 'FATAL', 'ERROR', 'WARNING',
          'INFO' or 'DEBUG'

    Returns:
        int: A valid python log level number
    """
    allowed_levels = [
        'CRITICAL',
        'FATAL',
        'ERROR',
        'WARN',
        'WARNING',
        'INFO',
        'DEBUG'
    ]

    if level not in allowed_levels:
        # TODO Log a warn. Using a default logger?
        level = 'INFO'

    return logging.getLevelName(level)


def init_logging():
    def_level = parse_log_level(cfg.log_level())
    con_level = parse_log_level(cfg.log_level_console())
    file_level = parse_log_level(cfg.log_level_file())

    logger = logging.getLogger(_LOGGER_NAME)
    logger.setLevel(def_level)

    # Console handler setup
    _ch = logging.StreamHandler()
    _ch.setLevel(con_level)
    _ch.setFormatter(_FORMATTER)
    logger.addHandler(_ch)

    # Rotating file handler setup
    _fh = RotatingFileHandler(
        os.path.join(cfg.logs_path(), _LOG_FILENAME),
        maxBytes=1024 * 1024 * 50,
        backupCount=5,
        encoding='utf-8'
    )
    _fh.setLevel(file_level)
    _fh.setFormatter(_FORMATTER)
    logger.addHandler(_fh)
