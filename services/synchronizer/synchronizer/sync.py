import os
import sys

# If package was not imported from other module
# and package is not been installed yet
if not __package__ and not hasattr(sys, "frozen"):
    synchronizer_root = os.path.dirname(
        os.path.dirname(os.path.abspath(__file__))
    )
    sys.path.insert(0, os.path.realpath(synchronizer_root))

import synchronizer.messaging.client as msg_client
from synchronizer.db.models import DBHttpSource
from synchronizer.sync_logging import logger


def sync_http_sources():
    logger.info('Starting sync of http sources')
    msg_client.sync_http_sources(DBHttpSource.objects.all())


if __name__ == "__main__":
    sync_http_sources()


