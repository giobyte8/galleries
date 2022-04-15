from distutils.command import config

import galleries.common.validators as validators
import galleries.common.futils as futils
from galleries.common.dao import get_files_galleries
from galleries.common.errors import PathNotFoundError, InvalidSourceTypeError
from galleries.common.models import FilesGallery, Source
from galleries.downloader.downloader import sync


def _verify_dir(path: str):
    try:
        validators.validate_dir_path(path)
    except PathNotFoundError:
        futils.mkdirs(path)


def _verify_gallery_dirs(gallery: FilesGallery):
    _verify_dir(gallery.abs_path())


def _validte_source(source: Source):
    if source.type not in ['http', 'file', 'git']:
        raise InvalidSourceTypeError(source.type)


def do_sync():
    validators.validate_env()

    for gallery in get_files_galleries():
        _verify_gallery_dirs(gallery)
        for source in gallery.sources:
            _validte_source(source)

    sync(gallery)
