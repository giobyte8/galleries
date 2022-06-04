import os


class PathNotCreatedError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self, f'Path could not be created: { path }')

class PathNotAFileError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self, f'Path is not a file: { path }')

class PathNotADirectoryError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self, f'Path is not a directory: { path }')


def ensure_dir_existence(path: str) -> None:
    if os.path.exists(path):
        if not os.path.isdir(path):
            raise PathNotADirectoryError(path)
    else:
        os.makedirs(path)
        if not os.path.exists(path):
            raise PathNotCreatedError(path)

def remove(path: str, validate_existence=False) -> bool:
    """Removes given file from storage

    Args:
        path (str): Path of file to be deleted
        validate_existence (bool, optional): If true, file existence will be \
            validated before trying to delete it and exception will raise if \
            file does not exists. Defaults to False.

    Raises:
        PathNotAFileError: Raised in case that validate_existence is true and \
            file was not found

    Returns:
        bool: True if file was not found after removal (successfully deleted)
    """
    if validate_existence and not os.path.isfile(path):
        raise PathNotAFileError(path)

    try:
        os.remove(path)
    except FileNotFoundError:
        pass

    return not os.path.isfile(path)
