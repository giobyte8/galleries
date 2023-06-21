import os
from dotenv import load_dotenv

load_dotenv()


def rabbitmq_host():
    return os.getenv('RABBITMQ_HOST')


def rabbitmq_port():
    return os.getenv('RABBITMQ_PORT')


def rabbitmq_user():
    return os.getenv('RABBITMQ_USER')


def rabbitmq_pass():
    return os.getenv('RABBITMQ_PASS')


def amqp_exchange():
    return os.getenv('AMQP_EXCHANGE')


def amqp_q_sync_http_src_orders():
    return os.getenv('AMQP_Q_SYNC_HTTP_SRC_ORDERS')


def amqp_q_file_downloaded():
    return os.getenv('AMQP_Q_FILE_DOWNLOADED')


def amqp_q_file_download_skipped():
    return os.getenv('AMQP_Q_FILE_DOWNLOAD_SKIPPED')


def amqp_q_source_synchronized():
    return os.getenv('AMQP_Q_SOURCE_SYNCHRONIZED')


def config_path():
    return os.getenv('CONFIG_PATH')


def content_root():
    return os.getenv('CONTENT_ROOT')


def runtime_path():
    return os.getenv('RUNTIME_PATH')


def logs_path():
    return os.path.join(runtime_path(), 'logs')


def log_level():
    return os.getenv('LOG_LEVEL', 'error')
