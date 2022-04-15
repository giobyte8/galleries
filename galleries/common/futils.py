import os
from galleries.common.errors import (
    PathAlreadyExistsError,
    PathNotCreatedError
)


def mkdirs(path: str, exists_ok = True):
    if os.path.exists(path):
        if not exists_ok:
            raise PathAlreadyExistsError(path)
    else:
        os.makedirs(path)
        if not os.path.exists(path):
            raise PathNotCreatedError(path)
