import json
import os
import subprocess
import uuid

import http_downloader.config as cfg
from http_downloader import futils
from http_downloader.dl_logging import logger


def download(source_id: int, url: str, dst_path: str) -> None:
    futils.ensure_dir_existence(dst_path)
    gl_config_file = _prepare_config_file(source_id)

    logger.info(
        'Starting download for source: %s from url: %s',
        source_id,
        url
    )

    # Print execyted command for debugging purposes
    #cmd = 'gallery-dl -D {} -c {} {}'.format(dst_path, gl_config_file, url)
    #logger.debug('Executing: %s', cmd)

    # REMOVE -q param for more verbose output from gallery-dl subprocess
    p = subprocess.run(['gallery-dl', '-q', '-D', dst_path, '-c', gl_config_file, url])

    # Comment this line for debugging purposes
    os.remove(gl_config_file)


def _prepare_config_file(source_id: int):
    logger.debug(
        'Preparing gallery-dl config file for source: %s',
        source_id
    )

    gdl_cfg_file = GDLConfigFileBuilder().\
        on_file_skipped_hook(source_id).\
        on_file_downloaded_hook(source_id).\
        build()

    return gdl_cfg_file


class GDLConfigFileBuilder:
    """A builder utility to construct the 'gallery-dl' config json
    """

    def __init__(self) -> None:

        # Path where config file is gonna be saved
        self.gdl_run_conf_path = os.path.join(
            cfg.runtime_path(),
            'gallery-dl-config/'
        )

        # Template gdl config file
        conf_template_path = os.path.join(
            cfg.config_path(),
            'gallery-dl.conf.json'
        )

        # Validate config template and runtime conf directory existence
        futils.ensure_dir_existence(self.gdl_run_conf_path)
        futils.assert_file_exists(conf_template_path)

        # Load config template as a dict
        with open(conf_template_path, 'r') as conf_template:
            self.j_config = json.loads(conf_template.read())

    def build(self) -> str:
        cfg_file_path = os.path.join(
            self.gdl_run_conf_path,
            f'{ str(uuid.uuid4()) }.json'
        )

        with open(cfg_file_path, 'w') as cfg_file:
            cfg_file.write(json.dumps(self.j_config))

        return cfg_file_path

    def on_file_skipped_hook(self, source_id: int):
        """Setup postprocessor to invoke every time a file download is skipped
            from given source. Usually files are skipped due to already present
            in target directory (Previously downloaded)

        Args:
            source_id (int): Id of source which skipped file belongs
        """
        postprocessors = self.j_config['extractor']['postprocessors']

        for postprocessor in postprocessors:
            if postprocessor['event'] == 'skip':
                postprocessor['command'] = [
                    'python',
                    'http_downloader/hooks/on_file_skipped.py',
                    str(source_id),
                    '{_filename}'
                ]
                break

        return self

    def on_file_downloaded_hook(self, source_id: int):
        """Setup postprocessor to invoke every time a file is downloaded
            from given source.

        Args:
            source_id (int): Id of source which file belongs
        """
        postprocessors = self.j_config['extractor']['postprocessors']

        for postprocessor in postprocessors:
            if postprocessor['event'] == 'after':
                postprocessor['command'] = [
                    'python',
                    'http_downloader/hooks/on_file_downloaded.py',
                    str(source_id),
                    '{_filename}'
                ]
            break

        return self
