import galleries.downloader.gdl_wrapper as gdl_wrapper
from galleries.common.models import FilesGallery, HttpSource, RemoteStatus
from galleries.db import gallery_dao, gallery_file_dao
from galleries.downloader.hooks import downloader_hooks


def sync_all_galleries() -> None:
    """Syncs all galleries with its remote sources

    Adds to cache:
        - Every item of every source that already exists
          in downloaded gallery files

    Triggers:
        - Event for every downloaded file of every gallery
        - Event for every gallery that has been processed
    """
    for gallery in gallery_dao.all():
        print(f'Syncing gallery: { gallery.name }')

        for source in gallery.sources:
            if isinstance(source, HttpSource):
                print(f'Syncing source: { source.url }')

                if source.sync_remote_deletes:
                    gallery_file_dao.set_remote_status_by_gallery(
                        gallery._id,
                        RemoteStatus.UNKNOWN
                    )
                _sync_http(gallery, source)

        downloader_hooks.on_gallery_downloaded(gallery)


def _sync_http(gallery: FilesGallery, source: HttpSource):
    gdl_wrapper.download(gallery, source)
