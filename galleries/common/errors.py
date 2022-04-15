
class InvalidSourceTypeError(Exception):
    def __init__(self, source_type: str) -> None:
        Exception.__init__(self)
        self.message = f'Unsuppoerted source type: { source_type }'

class NonWritablePathError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Unwritable path: { path }'

class PathAlreadyExistsError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Path already exists: { path }'

class PathNotCreatedError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Path could not be created: { path }'

class PathNotDirectoryError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Path is not a directory: { path }'

class PathNotFoundError(Exception):
    def __init__(self, path: str) -> None:
        Exception.__init__(self)
        self.message = f'Path does not exists: { path }'
