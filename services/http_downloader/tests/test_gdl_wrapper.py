import pytest
import tempfile

from http_downloader import gdl_wrapper
from http_downloader.gdl_wrapper import GDLCookies


@pytest.fixture
def tmp_dir():
    return tempfile.TemporaryDirectory()


def test_load_cookies():
    j_cookies = gdl_wrapper._load_cookies()
    assert len(j_cookies) > 0
    assert GDLCookies.PINTEREST_SESSION in j_cookies


def test_set_cookies():
    """Integration test for cookies setup
    """

    # Test extractor config
    j_extractor = {
        'pinterest': {
            'cookies': {}
        }
    }

    # Use temporary dir for saving config
    gdl_wrapper._set_cookies(j_extractor)
    j_pin_cookies = j_extractor['pinterest']['cookies']

    # Use _load_cookies to read it
    j_cookies = gdl_wrapper._load_cookies()

    assert GDLCookies.PINTEREST_SESSION in j_pin_cookies

    expected_pin_sess = j_cookies[GDLCookies.PINTEREST_SESSION]
    actual_pin_ses = j_pin_cookies[GDLCookies.PINTEREST_SESSION]
    assert actual_pin_ses == expected_pin_sess

