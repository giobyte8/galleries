import os
import sys

# If package was not imported from other module
# and package is not been installed yet
if not __package__ and not hasattr(sys, "frozen"):
    parent_pkg = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    gl_root = os.path.dirname(parent_pkg)

    sys.path.insert(0, os.path.realpath(gl_root))

from galleries.common.validators import validate_env
from galleries.downloader.downloader import sync_all_galleries


if __name__ == "__main__":
    validate_env()
    sync_all_galleries()
