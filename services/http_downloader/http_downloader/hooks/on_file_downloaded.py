#!python
#
# This script is executed directly by downloading tools
# everytime a file has been downloaded from a source
#

import os
import sys

# Add project root to sys.path
if __name__ == '__main__':
    dl_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    dl_root = os.path.dirname(dl_root)
    sys.path.insert(0, os.path.realpath(dl_root))

from http_downloader.messaging import producer


source_id = sys.argv[1]
filename = sys.argv[2]

producer.send_file_downloaded(source_id, filename)
