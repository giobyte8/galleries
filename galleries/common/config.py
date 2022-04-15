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


def config_path():
    return os.getenv('CONFIG_PATH')


def runtime_path():
    return os.getenv('RUNTIME_PATH')


def galleries_content_root():
    return os.getenv('GALLERIES_CONTENT_ROOT')