import os
import logging
from logging.handlers import RotatingFileHandler

import http_downloader.config as cfg
import http_downloader.futils as futils


_LOGGER_NAME = 'http_downloader'
_LOG_FILENAME = f'{_LOGGER_NAME}.log'

logger = logging.getLogger(_LOGGER_NAME)
logger.setLevel(logging.DEBUG)

_formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

# Console handler
_ch = logging.StreamHandler()
_ch.setLevel(logging.DEBUG)
_ch.setFormatter(_formatter)
logger.addHandler(_ch)

# Ensure logs path existence
futils.ensure_dir_existence(cfg.logs_path())

# Rotating file handler
_fh = RotatingFileHandler(
    os.path.join(cfg.logs_path(), _LOG_FILENAME),
    maxBytes=1024 * 1024 * 50,
    backupCount=5,
    encoding='utf-8'
)
_fh.setLevel(logging.INFO)
_fh.setFormatter(_formatter)
logger.addHandler(_fh)
