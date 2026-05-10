import importlib.util
import pathlib
import unittest


MODULE_PATH = pathlib.Path(__file__).resolve().parents[1] / "koreader_page_turner.py"


def load_app_module():
    spec = importlib.util.spec_from_file_location("koreader_page_turner", MODULE_PATH)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class FakeResponse:
    def __init__(self, error=None):
        self.error = error
        self.raise_called = False

    def raise_for_status(self):
        self.raise_called = True
        if self.error:
            raise self.error


class HttpHelpersTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.app = load_app_module()

    def test_build_koreader_url_uses_base_url_without_duplicate_slash(self):
        url = self.app.build_koreader_url(
            "http://192.168.1.8:8080/",
            "/koreader/device/screen/bb",
        )

        self.assertEqual(url, "http://192.168.1.8:8080/koreader/device/screen/bb")

    def test_ensure_success_status_calls_raise_for_status(self):
        response = FakeResponse()

        returned = self.app.ensure_success_status(response)

        self.assertIs(returned, response)
        self.assertTrue(response.raise_called)

    def test_ensure_success_status_propagates_http_errors(self):
        response = FakeResponse(RuntimeError("server error"))

        with self.assertRaises(RuntimeError):
            self.app.ensure_success_status(response)


if __name__ == "__main__":
    unittest.main()
