import importlib.util
import pathlib
import tkinter as tk
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

        self.assertEqual((width, height), (330, 270))

    def test_layout_scale_shrinks_at_compact_window_size(self):
        scale = self.app.get_layout_scale(
            window_width=220,
            window_height=180,
            dpi_scale=1.0,
        )

        self.assertEqual(scale, 0.72)

    def test_layout_scale_does_not_grow_above_default_size(self):
        scale = self.app.get_layout_scale(
            window_width=480,
            window_height=420,
            dpi_scale=1.0,
        )

        self.assertEqual(scale, 1.0)

    def test_theme_toggle_icon_changes_with_day_night_state(self):
        self.assertEqual(self.app.get_theme_toggle_icon_name("light"), "moon")
        self.assertEqual(self.app.get_theme_toggle_icon_name("dark"), "sun")

    def test_icon_asset_path_uses_nearest_scaled_bucket(self):
        path = self.app.get_icon_asset_relative_path("camera", "black", 20, 1.5)

        self.assertEqual(path, "assets/icons/30/camera-black.png")

    def test_icon_size_buckets_have_runtime_assets(self):
        assets_root = MODULE_PATH.parents[0] / "assets" / "icons"

        for size in self.app.ICON_SIZE_BUCKETS:
            with self.subTest(size=size):
                self.assertTrue((assets_root / str(size)).is_dir())
                self.assertTrue((assets_root / str(size) / "camera-black.png").is_file())


class ControlPageFocusTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.app = load_app_module()
        cls.app.pygame = None
        cls.app.PYGAME_IMPORT_ERROR = RuntimeError("disabled in GUI focus tests")

    def test_show_control_disables_hidden_ip_entry_and_moves_focus(self):
        root = tk.Tk()
        try:
            app = self.app.KOReaderRemoteApp(root)
            app.ip_var.set("192.168.1.8")
            root.update()
            app.entry_ip.focus_force()
            root.update()

            self.assertIs(root.focus_get(), app.entry_ip)

            app.connected = True
            app.show_control()
            root.update()

            self.assertEqual(str(app.entry_ip.cget("state")), tk.DISABLED)
            self.assertIsNot(root.focus_get(), app.entry_ip)
        finally:
            try:
                app.gamepad_running = False
            except UnboundLocalError:
                pass
            root.destroy()

    def test_show_login_reenables_ip_entry_for_editing(self):
        root = tk.Tk()
        try:
            app = self.app.KOReaderRemoteApp(root)
            app.entry_ip.configure(state=tk.DISABLED)

            app.show_login()
            root.update()

            self.assertEqual(str(app.entry_ip.cget("state")), tk.NORMAL)
        finally:
            try:
                app.gamepad_running = False
            except UnboundLocalError:
                pass
            root.destroy()

    def test_key_press_does_not_trigger_shortcuts_while_editing_ip_entry(self):
        root = tk.Tk()
        try:
            app = self.app.KOReaderRemoteApp(root)
            app.entry_ip.focus_force()
            root.update()
            calls = []
            app.trigger_action = calls.append

            class Event:
                keysym = "F5"

            result = app.on_key_press(Event())

            self.assertIsNone(result)
            self.assertEqual(calls, [])
        finally:
            try:
                app.gamepad_running = False
            except UnboundLocalError:
                pass
            root.destroy()


if __name__ == "__main__":
    unittest.main()
