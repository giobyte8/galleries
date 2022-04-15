import os

import galleries.common.config as cfg
from galleries.common.errors import (
    PathNotDirectoryError,
    PathNotFoundError
)
from galleries.common.models import FilesGallery


def validate_env():
    validate_dir_path(cfg.config_path())
    validate_dir_path(cfg.runtime_path())
    validate_dir_path(cfg.galleries_content_root())


def validate_dir_path(path: str) -> None:
    if not os.path.exists(path):
        raise PathNotFoundError(path)

    if not os.path.isdir(path):
        raise PathNotDirectoryError(path)


def validate_file(path: str) -> None:
    if not os.path.isfile(path):
        raise PathNotFoundError(path)


def validate_files_gallery(gallery: FilesGallery):
    gl_path = gallery.abs_path()

    if os.path.exists(gl_path):
        if not os.path.isdir(gl_path):
            raise PathNotDirectoryError(gl_path)
    else:
        os.makedirs(gl_path)
