import importlib.util
import pathlib
import unittest


MODULE_PATH = pathlib.Path(__file__).resolve().parents[1] / "koreader_page_turner.py"


def load_app_module():
    spec = importlib.util.spec_from_file_location("koreader_page_turner", MODULE_PATH)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class UiScalingAndIconsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.app = load_app_module()

    def test_scale_ui_value_keeps_minimum_one_pixel(self):
        self.assertEqual(self.app.scale_ui_value(0, 1.5), 1)
        self.assertEqual(self.app.scale_ui_value(20, 1.5), 30)

    def test_configured_window_size_uses_logical_dpi_values(self):
        width, height = self.app.get_configured_window_size(
            {
                "window_width_dp": 300,
                "window_height_dp": 280,
            },
            1.5,
        )

        self.assertEqual((width, height), (450, 420))

    def test_configured_window_size_clamps_legacy_values_to_scaled_minimum(self):
        width, height = self.app.get_configured_window_size(
            {
                "window_width": 250,
                "window_height": 220,
            },
            1.5,
        )

        self.assertEqual((width, height), (420, 360))

    def test_theme_toggle_icon_changes_with_day_night_state(self):
        self.assertEqual(self.app.get_theme_toggle_icon_name("light"), "moon")
        self.assertEqual(self.app.get_theme_toggle_icon_name("dark"), "sun")

    def test_icon_asset_path_uses_nearest_scaled_bucket(self):
        path = self.app.get_icon_asset_relative_path("camera", "black", 20, 1.5)

        self.assertEqual(path, "assets/icons/30/camera-black.png")


if __name__ == "__main__":
    unittest.main()
