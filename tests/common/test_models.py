from galleries.common.models import FilesGallery

class TestFilesGallery:
    def test_abs_path(self):
        gallery = FilesGallery(
            '123',
            'Random',
            'files',
            'random',
            True,
            'random',
            True,
            []
        )

        assert gallery.abs_path() == '/tmp/random'

    def test_rel_path(self):
        gallery = FilesGallery(
            '123',
            'Random',
            'files',
            '/random',
            False,
            '/random/thumbs',
            True,
            []
        )

        assert gallery.abs_path() == '/random'