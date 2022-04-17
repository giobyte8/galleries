import galleries.common.queues as queues
from galleries.common.models import FilesGallery


def on_gallery_downloaded(gallery: FilesGallery) -> None:
    """This hook notifies the system that all sources in a gallery
    had been processed.

    Args:
        gallery (FilesGallery): Gallery that was processed
    """
    queues.queue_downloaded_gallery(gallery._id)
