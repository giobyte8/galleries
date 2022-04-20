import os
from bson.objectid import ObjectId
from dataclasses import dataclass, field
from typing import Union

import galleries.common.config as cfg


@dataclass
class HttpSource:
    _id: ObjectId
    url: str
    sync_remote_deletes: bool

@dataclass
class GitSource:
    _id: ObjectId
    url: str
    path_inside_repo: str
    sync_remote_deletes: bool

@dataclass
class LocalSource:
    _id: ObjectId
    url: str
    sync_remote_deletes: bool

@dataclass
class ResizeOperation:
    w: int
    h: int
    verify_min_width: bool = False
    verify_min_height: bool = False

@dataclass
class CropOperation:
    w: int
    h: int
    start_x: int
    start_y: int

class Transformation:
    name: str
    path: str
    operations: list[Union[ResizeOperation, CropOperation]]
    based_on: str = None

@dataclass
class FilesGallery:
    _id: ObjectId
    name: str
    type: str
    path: str
    transformations_path: str
    transformations_path_relative_to_originals: bool
    sources: list[Union[HttpSource, GitSource, LocalSource]]
    transformations: list[Transformation]

    def abs_path(self) -> str:
        root = cfg.galleries_content_root()
        return os.path.join(root, self.path.strip('/'))

    def abs_trans_path(self) -> str:
        if self.transformations_path_relative_to_originals:
            return os.path.join(
                self.abs_path(),
                self.transformations_path.strip('/')
            )

        root = cfg.galleries_transformations_root()
        return os.path.join(
                root,
                self.transformations_path.strip('/')
            )

@dataclass
class TransformedVersion:
    name: str
    rel_file_path: str
    w: int
    h: int

@dataclass
class GalleryFile:
    gallery_id: ObjectId
    source_id: ObjectId
    filename: str
    _id: ObjectId = None
    deleted_on_remote: str = 'n'
    transformed_versions: list[TransformedVersion] = field(default_factory=list)
