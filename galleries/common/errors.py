
class PathAlreadyExistsError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self, f'Path already exists: { path }')

class PathNotCreatedError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self, f'Path could not be created: { path }')

class PathNotDirectoryError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self, f'Path is not a directory: { path }')

class PathNotFoundError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self, f'Path does not exists: { path }')
