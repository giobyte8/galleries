import json
import pytest
import tempfile

from http_downloader.gdl_wrapper import GDLConfigFileBuilder


@pytest.fixture
def tmp_dir():
    return tempfile.TemporaryDirectory()


class TestGDLConfigFileBuilder:
    def test_on_file_skipped_hook(self):
        src_id = 76543
        cfg_file_path = GDLConfigFileBuilder()\
            .on_file_skipped_hook(src_id)\
            .build()

        with open(cfg_file_path, 'r') as cfg_file:
           j_config = json.loads(cfg_file.read())
        pps = j_config['extractor']['postprocessors']

        skip_pp = None
        for pp in pps:
            if pp['event'] == 'skip':
                skip_pp = pp
        assert skip_pp is not None

        # Assert third argument for command is source id
        assert skip_pp['command'][2] == str(src_id)

    def test_on_file_downloaded_hook(self):
        src_id = 123
        cfg_file_path = GDLConfigFileBuilder()\
            .on_file_downloaded_hook(src_id)\
            .build()

        with open(cfg_file_path, 'r') as cfg_file:
           j_config = json.loads(cfg_file.read())
        pps = j_config['extractor']['postprocessors']

        after_pp = None
        for pp in pps:
            if pp['event'] == 'after':
                after_pp = pp
        assert after_pp is not None

        # Assert third argument for command is source id
        assert after_pp['command'][2] == str(src_id)
