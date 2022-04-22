#!python
#
# Consumes events for downloaded galleries and takes
# care of delete all files that don't belongs to remote
# gallery anymore
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
import galleries.services.gallery_file_service as gl_file_service
from galleries.common.queues import DOWNLOADED_GALLERIES_QUEUE


def on_message(channel: BlockingChannel, basic_deliver, props, body):
    print(f'Received message: {body}')
    j_body = json.loads(body.decode('utf-8'))
    gallery_id = ObjectId(j_body['gallery_id'])

    # Mark files that are still part of remote gallery
    # as part of local gallery
    gl_file_service.set_download_skipped_items_as_found(gallery_id)

    # Delete files that don't belongs to gallery anymore
    # in reasonable batches
    gl_file_service.delete_remote_unknown_files(gallery_id)

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
channel.basic_consume(DOWNLOADED_GALLERIES_QUEUE, on_message)
try:
    channel.start_consuming()
except KeyboardInterrupt:
    channel.stop_consuming()
conn.close()
