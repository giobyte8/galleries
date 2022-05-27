import os
import sys

# If package was not imported from other module
# and package is not been installed yet
if not __package__ and not hasattr(sys, "frozen"):
    synchronizer_root = os.path.dirname(
        os.path.dirname(os.path.abspath(__file__))
    )
    sys.path.insert(0, os.path.realpath(synchronizer_root))

from synchronizer.db import http_source_dao


if __name__ == '__main__':
    src = http_source_dao.all()
    print(src)