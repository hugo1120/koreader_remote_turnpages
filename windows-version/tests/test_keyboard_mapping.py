import importlib.util
import pathlib
import unittest


MODULE_PATH = pathlib.Path(__file__).resolve().parents[1] / "koreader_page_turner.py"


def load_app_module():
    spec = importlib.util.spec_from_file_location("koreader_page_turner", MODULE_PATH)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class KeyboardMappingTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.app = load_app_module()

    def test_default_keyboard_mapping_includes_device_actions(self):
        mapping = self.app.normalize_action_mapping(
            None,
            self.app.DEFAULT_KEYBOARD_MAPPING,
        )

        self.assertEqual(mapping["refresh"], ["F5"])
        self.assertEqual(mapping["rotate"], ["F6"])
        self.assertEqual(mapping["screenshot"], ["F7"])

    def test_custom_keyboard_mapping_accepts_readable_aliases(self):
        mapping = self.app.normalize_action_mapping(
            {
                "refresh": ["PageUp"],
                "rotate": "F8",
                "screenshot": ["PrintScreen"],
            },
            self.app.DEFAULT_KEYBOARD_MAPPING,
            self.app.KEYBOARD_KEY_ALIASES,
        )

        self.assertEqual(mapping["refresh"], ["Prior"])
        self.assertEqual(mapping["rotate"], ["F8"])
        self.assertEqual(mapping["screenshot"], ["Print"])

    def test_get_mapped_action_returns_first_matching_action(self):
        mapping = {
            "refresh": ["F5"],
            "rotate": ["F6"],
            "screenshot": ["F7"],
        }

        self.assertEqual(self.app.get_mapped_action(mapping, "F6"), "rotate")
        self.assertIsNone(self.app.get_mapped_action(mapping, "F8"))


if __name__ == "__main__":
    unittest.main()
