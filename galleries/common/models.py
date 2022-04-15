import os
from bson.objectid import ObjectId
from dataclasses import dataclass

import galleries.common.config as cfg


@dataclass
class Source:
    _id: ObjectId
    type: str
    url: str
    sync_remote_deletes: bool

@dataclass
class FilesGallery:
    _id: ObjectId
    name: str
    type: str
    path: str
    path_is_relative: bool
    transformations_path: str
    transformations_path_is_relative: bool
    sources: list[Source]

    def abs_path(self) -> str:
        if not self.path_is_relative:
            return self.path

        root = cfg.galleries_content_root()
        return os.path.join(root, self.path.strip('/'))

    def abs_transformations_path(self) -> str:
        # TODO Use different env variable in order to allow different content roots
        pass
