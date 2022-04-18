import json
from bson.objectid import ObjectId
from galleries.common.models import (
    CropOperation,
    GitSource,
    HttpSource,
    LocalSource,
    ResizeOperation
)
from galleries.common.schemas import (
    SourceSchema,
    TransOpSchema
)


source_schema = SourceSchema()
trans_op_schema = TransOpSchema()


def test_load_resize_operation():
    raw_resize_op = """
        {
            "type": "resize",
            "w": 250,
            "h": 250,
            "verify_min_width": false,
            "verify_min_height": true
        }
    """
    j_resize_op = json.loads(raw_resize_op)
    resize_op = trans_op_schema.load(j_resize_op)

    assert isinstance(resize_op, ResizeOperation)
    assert resize_op.w == 250
    assert resize_op.h == 250
    assert not resize_op.verify_min_width
    assert resize_op.verify_min_height


def test_load_crop_operation():
    raw_crop_op = """
        {
            "type": "crop",
            "w": 250,
            "h": 250,
            "start_x": 50,
            "start_y": 50
        }
    """
    j_crop_op = json.loads(raw_crop_op)
    crop_op = trans_op_schema.load(j_crop_op)

    assert isinstance(crop_op, CropOperation)
    assert crop_op.w == 250
    assert crop_op.h == 250
    assert crop_op.start_x == 50
    assert crop_op.start_y == 50


def test_load_http_source():
    raw_http_source = """
        {
            "type": "http",
            "url": "http://example.com",
            "sync_remote_deletes": false
        }
    """
    j_http_s = json.loads(raw_http_source)
    j_http_s['_id'] = ObjectId()

    source =  source_schema.load(j_http_s)
    assert isinstance(source, HttpSource)
    assert source.url == 'http://example.com'


def test_load_git_source():
    raw_source = """
        {
            "type": "git",
            "url": "http://example.com/repo.git",
            "path_inside_repo": "/path/inside/git",
            "sync_remote_deletes": true
        }
    """
    j_source = json.loads(raw_source)
    j_source['_id'] = ObjectId()

    source =  source_schema.load(j_source)
    assert isinstance(source, GitSource)
    assert source.url == 'http://example.com/repo.git'


def test_load_local_source():
    raw_source = """
        {
            "type": "local",
            "url": "file://media/randomstore/images",
            "sync_remote_deletes": true
        }
    """
    j_source = json.loads(raw_source)
    j_source['_id'] = ObjectId()

    source =  source_schema.load(j_source)
    assert isinstance(source, LocalSource)
    assert source.url == 'file://media/randomstore/images'


def test_load_files_gallery():
    pass


def test_load_objects_gallery():
    pass
