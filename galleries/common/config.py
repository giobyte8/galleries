import os
from dotenv import load_dotenv


load_dotenv()


def db_host():
    return os.getenv('DB_HOST')


def db_port():
    return int(os.getenv('DB_PORT'))


def db_username():
    return os.getenv('DB_USERNAME')


def db_password():
    return os.getenv('DB_PASSWORD')


def cache_host():
    return os.getenv('CACHE_HOST')


def cache_port():
    return os.getenv('CACHE_PORT')


def config_path():
    return os.getenv('CONFIG_PATH')


def rabbitmq_host():
    return os.getenv('RABBITMQ_HOST')


def rabbitmq_port():
    return os.getenv('RABBITMQ_PORT')


def rabbitmq_user():
    return os.getenv('RABBITMQ_USER')


def rabbitmq_pass():
    return os.getenv('RABBITMQ_PASS')


def runtime_path():
    return os.getenv('RUNTIME_PATH')


def log_level():
    return os.getenv('LOG_LEVEL', 'error')


def logs_path():
    return os.path.join(runtime_path(), 'logs')


def galleries_content_root():
    return os.getenv('GALLERIES_CONTENT_ROOT')


def galleries_transformations_root():
    return os.getenv('GALLERIES_TRANSFORMATIONS_ROOT')
