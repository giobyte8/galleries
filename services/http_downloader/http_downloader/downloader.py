import os
import sys

# If package was not imported from other module
# and package is not been installed yet
if not __package__ and not hasattr(sys, "frozen"):
    downloader_root = os.path.dirname(
        os.path.dirname(os.path.abspath(__file__))
    )
    sys.path.insert(0, os.path.realpath(downloader_root))

from http_downloader.messaging import sync_sources_consumer


if __name__ == '__main__':
    sync_sources_consumer.start_consumer()
