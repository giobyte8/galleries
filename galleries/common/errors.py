
class NonWritablePathError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Unwritable path: { path }'

class PathNotDirectoryError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Path is not a directory: { path }'

class PathNotFoundError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Path does not exists: { path }'
