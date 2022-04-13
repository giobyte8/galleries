import bson
import typing
from marshmallow import (
    EXCLUDE,
    Schema,
    ValidationError,
    fields,
    post_load
)

from galleries.common.models import Source, FilesGallery


class ObjectIdField(fields.Field):
    def _serialize(self, value: typing.Any, attr: str, obj: typing.Any, **kwargs):
        if value is None:
            return None
        return str(value)

    def _deserialize(self, value: typing.Any, attr: str, data: typing.Mapping[str, typing.Any], **kwargs):
        try:
            return bson.ObjectId(value)
        except (TypeError, bson.errors.InvalidId):
            raise ValidationError('Invalid ObjectId')

class SourceSchema(Schema):
    type = fields.Str()
    url = fields.Url()
    sync_remote_deletes = fields.Bool()

    @post_load
    def to_model(self, data, **kwargs):
        return Source(**data)

class FilesGallerySchema(Schema):
    _id = ObjectIdField()
    name = fields.Str()
    type = fields.Str()
    path = fields.Str()
    path_is_relative = fields.Bool()
    transformations_path = fields.Str()
    transformations_path_is_relative = fields.Bool()
    sources = fields.List(fields.Nested(SourceSchema))

    @post_load
    def to_model(self, data, **kwargs):
        return FilesGallery(**data)

    class Meta:
        unknown = EXCLUDE
