import os

import galleries.common.config as cfg
from galleries.common.errors import (
    PathNotDirectoryError,
    PathNotFoundError
)
from galleries.common.models import FilesGallery


def validate_env():
    validate_dir_exists(cfg.config_path())
    validate_dir_exists(cfg.runtime_path())
    validate_dir_exists(cfg.galleries_content_root())
    validate_dir_exists(cfg.galleries_transformations_root())


def validate_dir_exists(path: str) -> None:
    if not os.path.exists(path):
        raise PathNotFoundError(path)

    if not os.path.isdir(path):
        raise PathNotDirectoryError(path)


def validate_file_exists(path: str) -> None:
    if not os.path.isfile(path):
        raise PathNotFoundError(path)

# TODO Move this to futils as 'ensure_dir_existence'
def validate_files_gallery(gallery: FilesGallery):
    gl_path = gallery.abs_path()

    if os.path.exists(gl_path):
        if not os.path.isdir(gl_path):
            raise PathNotDirectoryError(gl_path)
    else:
        os.makedirs(gl_path)
