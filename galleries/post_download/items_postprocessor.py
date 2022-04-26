#!python
#
# Consumes events for downloaded gallery items, applies
# configured transformations for gallery and adds the
# item to gallery items collection
#

import json
import os
import pika
import sys

from bson.objectid import ObjectId
from pika.adapters.blocking_connection import BlockingChannel

# Add project root to sys.path
if __name__ == '__main__':
    project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    project_root = os.path.dirname(project_root)
    sys.path.insert(0, os.path.realpath(project_root))

import galleries.common.config as cfg
from galleries.db import gallery_file_dao
from galleries.common.models import GalleryFile
from galleries.common.queues import DOWNLOADED_FILES_QUEUE


def on_message(channel: BlockingChannel, basic_deliver, props, body):
    print(f'Postprocessing: { body.decode("utf-8") }')
    j_body = json.loads(body.decode('utf-8'))
    gl_file = GalleryFile(
        ObjectId(j_body['gallery_id']),
        ObjectId(j_body['source_id']),
        j_body['filename']
    )

    # TODO Apply transformations

    gallery_file_dao.save(gl_file)
    channel.basic_ack(basic_deliver.delivery_tag)


conn = pika.BlockingConnection(pika.ConnectionParameters(
    host=cfg.rabbitmq_host(),
    port=cfg.rabbitmq_port(),
    credentials=pika.PlainCredentials(
        cfg.rabbitmq_user(),
        cfg.rabbitmq_pass()
    )
))

channel = conn.channel()
channel.queue_declare(queue=DOWNLOADED_FILES_QUEUE)
channel.basic_consume(DOWNLOADED_FILES_QUEUE, on_message)

try:
    channel.start_consuming()
except KeyboardInterrupt:
    channel.stop_consuming()
conn.close()