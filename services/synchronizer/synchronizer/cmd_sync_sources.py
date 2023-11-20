#!/bin/python
#
# Triggers sync for all 'http_source' items found in database
#

import logging
import os
import sys

# If package was not imported from other module
# and package is not been installed yet
if not __package__ and not hasattr(sys, "frozen"):
    synchronizer_root = os.path.dirname(
        os.path.dirname(os.path.abspath(__file__))
    )
    sys.path.insert(0, os.path.realpath(synchronizer_root))

import synchronizer.services.http_source_service as http_src_svc


logger = logging.getLogger(__name__)


if __name__ == "__main__":
    logger.info('Starting sync of http sources')
    http_src_svc.sync_http_sources()


