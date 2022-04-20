import os
from galleries.common.errors import (
    PathNotDirectoryError,
    PathNotCreatedError
)


def ensure_dir_existence(path: str) -> None:
    if os.path.exists(path):
        if not os.path.isdir(path):
            raise PathNotDirectoryError(path)
    else:
        os.makedirs(path)
        if not os.path.exists(path):
            raise PathNotCreatedError(path)
