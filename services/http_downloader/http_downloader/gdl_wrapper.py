import json
import os
import subprocess
import uuid
from typing import List

import http_downloader.config as cfg
from http_downloader import futils
from http_downloader.dl_logging import logger


_CONF_TEMPLATE='gallery-dl.conf.template.json'
_conf_template_path=os.path.join(cfg.config_path(), _CONF_TEMPLATE)

_RUNTIME_PATH = cfg.runtime_path()
_gallery_dl_conf_path = os.path.join(_RUNTIME_PATH, 'gallery-dl-config')

def download(source_id: str, url: str, dst_path: str) -> None:
    futils.assert_file_exists(_conf_template_path)
    futils.ensure_dir_existence(_gallery_dl_conf_path)
    futils.ensure_dir_existence(dst_path)

    gl_config_file = _prepare_config_file(source_id)
    logger.info(
        'Starting download for source: %s from url: %s',
        source_id,
        url
    )
    p = subprocess.run(['gallery-dl', '-D', dst_path, '-c', gl_config_file, url])
    os.remove(gl_config_file)


def _prepare_config_file(source_id: str):
    logger.debug(
        'Preparing gallery-dl config file for source: %s',
        source_id
    )

    with open(_conf_template_path, 'r') as conf_template:
        gl_config = json.loads(conf_template.read())

    postprocessors = gl_config['extractor']['postprocessors']
    _set_on_file_downloaded_hook_call(source_id, postprocessors)
    _set_on_file_skipped_hook_call(source_id, postprocessors)

    source_config_path = os.path.join(
        _gallery_dl_conf_path,
        f'{ str(uuid.uuid4()) }.json'
    )

    with open(source_config_path, 'w') as source_config:
        source_config.write(json.dumps(gl_config))

    return source_config_path


def _set_on_file_skipped_hook_call(
    source_id: str,
    postprocessors: List[dict]
) -> None:
    for postprocessor in postprocessors:
        if postprocessor['event'] == 'skip':
            postprocessor['command'] = [
                'python',
                'http_downloader/hooks/on_file_skipped.py',
                source_id,
                '{_filename}'
            ]
            break


def _set_on_file_downloaded_hook_call(
    source_id: str,
    postprocessors: List[dict]
) -> None:
    for postprocessor in postprocessors:
        if postprocessor['event'] == 'after':
            postprocessor['command'] = [
                'python',
                'http_downloader/hooks/on_file_downloaded.py',
                source_id,
                '{_filename}'
            ]
