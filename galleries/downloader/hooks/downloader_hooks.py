import galleries.common.cache_dao as cache_dao
from galleries.common.models import FilesGallery


def on_gallery_downloaded(gallery: FilesGallery) -> None:
    # TODO Mark all skipped files like still belongs to remote source
    # TODO Delete all non skipeed files (Not belong to gallery anymore)

    for skipped_item in cache_dao.skipped_items(gallery._id):
        print(f'Skipped file: { skipped_item["filename"]}')
