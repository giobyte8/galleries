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

import galleries.common.cache_dao as cache
import galleries.common.config as cfg
from galleries.db import gallery_file_dao
from galleries.common.models import RemoteStatus
from galleries.common.queues import DOWNLOADED_GALLERIES_QUEUE


def on_message(channel: BlockingChannel, basic_deliver, props, body):
    print(f'Received message: {body}')
    j_body = json.loads(body.decode('utf-8'))
    gallery_id = ObjectId(j_body['gallery_id'])

    for skipped_item in cache.skipped_items(gallery_id):
        source_id = ObjectId(skipped_item['source_id'])
        filename = skipped_item['filename']
        gallery_file_dao.set_deleted_on_remote(
            gallery_id,
            source_id,
            filename,
            RemoteStatus.EXISTS
        )

    # TODO Get all gallery files with UNKNOWN remote status (paginated)
    # TODO Delete each item file and append item id to list of items to delete
    # TODO Delete page of items and go to next page


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
