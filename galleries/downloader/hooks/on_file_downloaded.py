#!python
#
# This script is executed directly by downloading tools
# everytime a file has been downloaded from a source
#
#

import os
import sys

# Add project root to sys.path
if __name__ == '__main__':
    project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    project_root = os.path.dirname(os.path.dirname(project_root))
    sys.path.insert(0, os.path.realpath(project_root))

from galleries.common.models import FilesGallery


gallery_id = sys.argv[1]
source_id = sys.argv[2]
filename = sys.argv[3]

print(f'Downloaded file: {filename}, for source: {source_id}, for gallery: {gallery_id}')
