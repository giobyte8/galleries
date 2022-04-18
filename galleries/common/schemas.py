import bson
import typing
from typing import Union
from marshmallow import (
    EXCLUDE,
    Schema,
    ValidationError,
    fields,
    post_load
)
from marshmallow_oneofschema import OneOfSchema

from galleries.common.models import (
    FilesGallery,
    HttpSource,
    GitSource,
    LocalSource,
    CropOperation,
    ResizeOperation
)


class ObjectIdField(fields.Field):
    def _serialize(self,
    value: typing.Any, attr: str, obj: typing.Any, **kwargs):
        if value is None:
            return None
        return str(value)

    def _deserialize(
        self,
        value: typing.Any,
        attr: str,
        data: typing.Mapping[str, typing.Any],
        **kwargs
    ):
        try:
            return bson.ObjectId(value)
        except (TypeError, bson.errors.InvalidId):
            raise ValidationError('Invalid ObjectId')

class HttpSourceSchema(Schema):
    _id = ObjectIdField()
    url = fields.Url()
    sync_remote_deletes = fields.Bool()

    @post_load
    def to_model(self, data, **kwargs):
        return HttpSource(**data)

class GitSourceSchema(Schema):
    _id = ObjectIdField()
    url = fields.Url()
    path_inside_repo = fields.Str()
    sync_remote_deletes = fields.Bool()

    @post_load
    def to_model(self, data, **kwargs):
        return GitSource(**data)

class LocalSourceSchema(Schema):
    _id = ObjectIdField()
    url = fields.Str()
    sync_remote_deletes = fields.Bool()

    @post_load
    def to_model(self, data, **kwargs):
        return LocalSource(**data)

class SourceSchema(OneOfSchema):
    type_schemas = {
        'git': GitSourceSchema,
        'http': HttpSourceSchema,
        'local': LocalSourceSchema
    }

    def get_obj_type(self, obj):
        if isinstance(obj, HttpSource):
            return 'http'
        elif isinstance(obj, GitSource):
            return 'git'
        elif isinstance(obj, LocalSource):
            return 'local'

class ResizeOpSchema(Schema):
    w = fields.Int()
    h = fields.Int()
    verify_min_width = fields.Bool()
    verify_min_height = fields.Bool()

    @post_load
    def to_model(self, data, **kwargs):
        return ResizeOperation(**data)

class CropOpSchema(Schema):
    w = fields.Int()
    h = fields.Int()
    start_x = fields.Int()
    start_y = fields.Int()

    @post_load
    def to_model(self, data, **kwargs):
        return CropOperation(**data)

class TransOpSchema(OneOfSchema):
    type_schemas = { 'resize': ResizeOpSchema, 'crop': CropOpSchema }

    def get_obj_type(self, obj):
        if isinstance(obj, ResizeOperation):
            return 'resize'
        elif isinstance(obj, CropOperation):
            return 'crop'
        else:
            raise Exception('Unknown object type: {obj.__class__.__name__}')

class TransformationSchema(Schema):
    name = fields.Str()
    path = fields.Str()
    operations = fields.List(fields.Nested(TransOpSchema))

class FilesGallerySchema(Schema):
    _id = ObjectIdField()
    name = fields.Str()
    type = fields.Str()
    path = fields.Str()
    transformations_path = fields.Str()
    transformations_path_relative_to_originals = fields.Bool()
    sources = fields.List(fields.Nested(SourceSchema))
    transformations = fields.List(fields.Nested(TransformationSchema))

    @post_load
    def to_model(self, data, **kwargs):
        return FilesGallery(**data)

    class Meta:
        unknown = EXCLUDE
