import os
import galleries.common.config as cfg
from galleries.common.models import FilesGallery

class TestFilesGallery:
    def test_abs_path(self):
        gallery = FilesGallery(
            '123',
            'Random',
            'files',
            'random',
            'random',
            True,
            [],
            []
        )

        assert gallery.abs_path() == '/tmp/random'

    def test_abs_trans_path(self):
        gallery = FilesGallery(
            '123',
            'Random',
            'files',
            '/random',
            '/random',
            False,
            [],
            []
        )

        expected_path = os.path.join(
            cfg.galleries_transformations_root(),
            'random'
        )

        assert gallery.abs_trans_path() == expected_path


    def test_rel_trans_path(self):
        gallery = FilesGallery(
            '123',
            'Random',
            'files',
            '/random',
            '/random_transformations/',
            True,
            [],
            []
        )

        expected_path = os.path.join(
            gallery.abs_path(),
            'random_transformations'
        )

        assert gallery.abs_trans_path() == expected_path