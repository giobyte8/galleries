import os

import galleries.common.config as cfg
from galleries.common.errors import (
    PathNotADirectoryError,
    PathNotFoundError
)


def validate_env():
    validate_dir_exists(cfg.config_path())
    validate_dir_exists(cfg.runtime_path())
    validate_dir_exists(cfg.galleries_content_root())
    validate_dir_exists(cfg.galleries_transformations_root())


def validate_dir_exists(path: str) -> None:
    if not os.path.exists(path):
        raise PathNotFoundError(path)

    if not os.path.isdir(path):
        raise PathNotADirectoryError(path)


def validate_file_exists(path: str) -> None:
    if not os.path.isfile(path):
        raise PathNotFoundError(path)
