#!/bin/python
#
# Starts consumers for receive and process synchronization
# events sent from downloader modules

import os
import sys

# If package was not imported from other module
# and package is not been installed yet
if not __package__ and not hasattr(sys, "frozen"):
    synchronizer_root = os.path.dirname(
        os.path.dirname(os.path.abspath(__file__))
    )
    sys.path.insert(0, os.path.realpath(synchronizer_root))

from synchronizer.messaging.sync_events_consumer import start_consumer


if __name__ == '__main__':
    start_consumer()
