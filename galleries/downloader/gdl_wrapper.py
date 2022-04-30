import json
import os
import subprocess
import uuid
from bson.objectid import ObjectId

import galleries.common.config as cfg
import galleries.common.futils as futils
import galleries.common.validators as validators
from galleries.common.models import FilesGallery, HttpSource
from galleries.downloader.dl_logging import logger


_CONF_TEMPLATE='gallery-dl.conf.template.json'
_conf_template_path=os.path.join(cfg.config_path(), _CONF_TEMPLATE)

_RUNTIME_PATH = cfg.runtime_path()
_gallery_dl_conf_path = os.path.join(_RUNTIME_PATH, 'gallery-dl-config')

def download(gallery: FilesGallery, source: HttpSource) -> None:
    validators.validate_file_exists(_conf_template_path)
    futils.ensure_dir_existence(_gallery_dl_conf_path)

    gl_config_file = _prepare_config_file(gallery._id, source._id)
    logger.info(
        'Starting download for gallery: %s, source: %s, url: %s',
        gallery._id,
        source._id,
        source.url
    )
    p = subprocess.run(['gallery-dl', '-D', gallery.abs_path(), '-c', gl_config_file, source.url])
    os.remove(gl_config_file)


def _prepare_config_file(gallery_id: ObjectId, source_id: ObjectId):
    logger.debug(
        'Preparing gallery-dl config file for gallery: %s and source %s',
        gallery_id,
        source_id
    )

    with open(_conf_template_path, 'r') as conf_template:
        gl_config = json.loads(conf_template.read())

    postprocessors = gl_config['extractor']['postprocessors']
    _set_on_file_downloaded_hook_call(gallery_id, source_id, postprocessors)
    _set_on_file_skipped_hook_call(gallery_id, source_id, postprocessors)

    source_config_path = os.path.join(
        _gallery_dl_conf_path,
        f'{ str(uuid.uuid4()) }.json'
    )

    with open(source_config_path, 'w') as source_config:
        source_config.write(json.dumps(gl_config))

    return source_config_path


def _set_on_file_skipped_hook_call(
    gallery_id: ObjectId,
    source_id: ObjectId,
    postprocessors: list[dict]
) -> None:
    for postprocessor in postprocessors:
        if postprocessor['event'] == 'skip':
            postprocessor['command'] = [
                'python',
                'galleries/downloader/hooks/on_file_skipped.py',
                str(gallery_id),
                str(source_id),
                '{_filename}'
            ]
            break


def _set_on_file_downloaded_hook_call(
    gallery_id: ObjectId,
    source_id: ObjectId,
    postprocessors: list[dict]
) -> None:
    for postprocessor in postprocessors:
        if postprocessor['event'] == 'after':
            postprocessor['command'] = [
                'python',
                'galleries/downloader/hooks/on_file_downloaded.py',
                str(gallery_id),
                str(source_id),
                '{_filename}'
            ]
