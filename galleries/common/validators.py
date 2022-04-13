import os

import galleries.common.config as cfg
from galleries.common.errors import (
    PathNotDirectoryError,
    PathNotFoundError
)


def validate_env():
    validate_dir_path(cfg.runtime_path())
    validate_dir_path(cfg.galleries_content_root())


def validate_dir_path(path: str, check_write_permission = True) -> None:
    if not os.path.exists(path):
        raise PathNotFoundError(path)

    if not os.path.isdir(path):
        raise PathNotDirectoryError(path)
