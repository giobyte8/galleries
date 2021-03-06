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
    return os.getenv('LOG_LEVEL', 'error')
