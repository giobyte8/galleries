import os
from dotenv import load_dotenv

load_dotenv()


def mysql_host():
    return os.getenv('MYSQL_HOST')


def mysql_port():
    return os.getenv('MYSQL_PORT')


def mysql_database():
    return os.getenv('MYSQL_DATABASE')


def mysql_user():
    return os.getenv('MYSQL_USER')


def mysql_password():
    return os.getenv('MYSQL_PASSWORD')


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


def redis_host():
    return os.getenv('REDIS_HOST')


def redis_port():
    return os.getenv('REDIS_PORT')


def content_root():
    return os.getenv('CONTENT_ROOT')


def transformations_root():
    return os.getenv('TRANSFORMATIONS_ROOT')


def runtime_path():
    return os.getenv('RUNTIME_PATH')


def logs_path():
    return os.path.join(runtime_path(), 'logs')

def log_level():
    return os.getenv('LOG_LEVEL', 'INFO')

def log_level_console():
    return os.getenv('LOG_LEVEL_CONSOLE', log_level())

def log_level_file():
    return os.getenv('LOG_LEVEL_FILE', log_level())


def otel_collector_endpoint():
    return os.getenv('OTEL_COLLECTOR_ENDPOINT')

def otel_svc_name():
    return os.getenv('OTEL_SERVICE_NAME')
