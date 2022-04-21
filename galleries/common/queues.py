import contextlib
import json
import pika
from bson.objectid import ObjectId
import galleries.common.config as cfg


DOWNLOADED_FILES_QUEUE='downloaded-files'
DOWNLOADED_GALLERIES_QUEUE='downloaded-galleries'


@contextlib.contextmanager
def _rb_channel():
    try:
        conn = pika.BlockingConnection(pika.ConnectionParameters(
            host=cfg.rabbitmq_host(),
            port=cfg.rabbitmq_port(),
            credentials=pika.PlainCredentials(
                cfg.rabbitmq_user(),
                cfg.rabbitmq_pass()
            )
        ))

        yield conn.channel()
        conn.close()
    except Exception as e:
        print(f'Rabbit connection failed: {e}')


def queue_downloaded_file(
    gallery_id: str,
    source_id: str,
    filename: str
) -> None:
    with _rb_channel() as channel:
        dl_item = {
            'gallery_id': gallery_id,
            'source_id': source_id,
            'filename': filename
        }

        channel.queue_declare(queue=DOWNLOADED_FILES_QUEUE)
        channel.basic_publish(
            exchange='',
            routing_key=DOWNLOADED_FILES_QUEUE,
            body=json.dumps(dl_item)
        )


def queue_downloaded_gallery(gallery_id: ObjectId) -> None:
    with _rb_channel() as channel:
        dl_gallery = { 'gallery_id': str(gallery_id) }

        channel.queue_declare(queue=DOWNLOADED_GALLERIES_QUEUE)
        channel.basic_publish(
            exchange='',
            routing_key=DOWNLOADED_GALLERIES_QUEUE,
            body=json.dumps(dl_gallery)
        )