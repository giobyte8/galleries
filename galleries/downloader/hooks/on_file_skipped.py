#!python
#
# This script is executed directly by downloading tools
# everytime a file has been skippd because was previously downloaded
#

import os
import sys

# Add project root to sys.path
if __name__ == '__main__':
    project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    project_root = os.path.dirname(os.path.dirname(project_root))
    sys.path.insert(0, os.path.realpath(project_root))

import galleries.common.cache_dao as cache


gallery_id = sys.argv[1]
source_id = sys.argv[2]
filename = sys.argv[3]

cache.add_skipped_file(gallery_id, source_id, filename)
