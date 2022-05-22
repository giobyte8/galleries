import os

import http_downloader.config as cfg


def source_content_abs_path(rel_path: str) -> str:
    """Generates the absolute path for source content by \
        using content path from env and given relative \
        content path

    Args:
        rel_path (str): Relative path for source content

    Returns:
        str: Absolute path to source content dir
    """
    return os.path.join(cfg.content_root(), rel_path.strip('/'))
