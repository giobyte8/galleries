from bson.objectid import ObjectId
import galleries.downloader.gdl_wrapper as gdl_wrapper
from galleries.common.models import FilesGallery, Source


def sync(gallery: FilesGallery) -> None:
    """Syncs gallery with its remote sources.

    Triggers:
        - Event for every downloaded file
        - Event for every skipped file
        - Event when all source files have been processed
        - Event when all gallery sources have been processed

    Args:
        gallery (FilesGallery): Gallery to sync
    """
    print(f'Syncing gallery: { gallery.name }')

    for source in gallery.sources:
        if source.sync_remote_deletes:
            # TODO Mark source items as '?'
            pass

        print(f'Syncing source: { source.url }')
        if source.type == 'http':
            sync_http(gallery, source)

def sync_http(gallery: FilesGallery, source: Source):
    gdl_wrapper.download(gallery, source)
