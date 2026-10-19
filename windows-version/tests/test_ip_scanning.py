import importlib.util
import pathlib
import tkinter as tk
import unittest
from unittest import mock


MODULE_PATH = pathlib.Path(__file__).resolve().parents[1] / "koreader_page_turner.py"


def load_app_module():
    spec = importlib.util.spec_from_file_location("koreader_page_turner_scan", MODULE_PATH)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class ParseIpInputTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.app = load_app_module()

    def test_full_ip_is_dialed_directly(self):
        self.assertEqual(self.app.parse_ip_input("192.168.2.15"), ("full", "192.168.2.15"))
        self.assertEqual(self.app.parse_ip_input(" 10.0.0.7 "), ("full", "10.0.0.7"))

    def test_three_octets_trigger_subnet_scan(self):
        for raw in ("192.168.2", "192.168.2.", "192.168.2.x", "192.168.2.X", "192.168.2.*"):
            with self.subTest(raw=raw):
                self.assertEqual(self.app.parse_ip_input(raw), ("scan", "192.168.2"))

    def test_two_octets_are_kept_for_local_adapter_completion(self):
        for raw in ("10.68", "10.68.", "10.68.x", "10.68.*"):
            with self.subTest(raw=raw):
                self.assertEqual(self.app.parse_ip_input(raw), ("scan", "10.68"))

    def test_trailing_zero_host_is_treated_as_subnet(self):
        self.assertEqual(self.app.parse_ip_input("192.168.2.0"), ("scan", "192.168.2"))

    def test_http_scheme_and_path_are_stripped(self):
        self.assertEqual(
            self.app.parse_ip_input("http://192.168.2.30/"),
            ("full", "192.168.2.30")
        )

    def test_invalid_input_returns_none(self):
        for raw in ("", "   ", "10", "192.168.2.15.1", "192.168.2.abc", "999.1.1.1"):
            with self.subTest(raw=raw):
                self.assertIsNone(self.app.parse_ip_input(raw))


class ScanPrefixTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.app = load_app_module()

    def test_three_octets_are_used_as_is(self):
        self.assertEqual(self.app.expand_scan_prefixes("192.168.2"), ["192.168.2"])

    def test_two_octets_are_completed_from_local_adapters(self):
        prefixes = self.app.expand_scan_prefixes("10.68", ["10.68.3.7", "192.168.2.5"])
        self.assertEqual(prefixes, ["10.68.3"])

    def test_two_octets_without_matching_adapter_yield_nothing(self):
        self.assertEqual(self.app.expand_scan_prefixes("10.68", ["192.168.2.5"]), [])


class SubnetScanTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.module = load_app_module()

    def setUp(self):
        self.root = tk.Tk()
        self.module.pygame = None
        self.module.PYGAME_IMPORT_ERROR = RuntimeError("disabled in scan tests")
        self.app = self.module.KOReaderRemoteApp(self.root)

    def tearDown(self):
        try:
            self.app.gamepad_running = False
        except AttributeError:
            pass
        self.root.destroy()

    def test_iter_subnet_hosts_covers_usable_range(self):
        hosts = self.module.iter_subnet_hosts("192.168.2")
        self.assertEqual(len(hosts), 254)
        self.assertEqual(hosts[0], "192.168.2.1")
        self.assertEqual(hosts[-1], "192.168.2.254")

    def test_scan_returns_first_confirmed_device_in_ip_order(self):
        with mock.patch.object(
            self.app,
            "probe_device",
            side_effect=lambda ip: ip == "192.168.2.200"
        ):
            found = self.app.scan_subnet("192.168.2")

        # 200 号之后没有别的命中，按 IP 顺序取唯一命中的设备
        self.assertEqual(found, "192.168.2.200")

    def test_every_host_is_probed_without_truncation(self):
        # 旧实现只验证前 16 个候选，装代理时 254 个地址全被误判为候选，真设备必然漏掉
        probed = []

        def fake_probe(ip, timeout=None):
            probed.append(ip)
            return ip == "192.168.2.240"

        with mock.patch.object(self.app, "probe_device", side_effect=fake_probe):
            self.assertEqual(self.app.scan_subnet("192.168.2"), "192.168.2.240")

        self.assertEqual(len(probed), 254)
        self.assertIn("192.168.2.240", probed)

    def test_lowest_ip_wins_when_multiple_devices_answer(self):
        with mock.patch.object(
            self.app,
            "probe_device",
            side_effect=lambda ip: ip in ("192.168.2.90", "192.168.2.9")
        ):
            self.assertEqual(self.app.scan_subnet("192.168.2"), "192.168.2.9")

    def test_two_octet_scan_uses_local_adapter_prefix(self):
        with mock.patch.object(self.module, "local_ipv4_addresses", return_value=["10.68.3.7"]), \
                mock.patch.object(self.app, "probe_device", side_effect=lambda ip: ip == "10.68.3.9"):
            self.assertEqual(self.app.scan_subnet("10.68"), "10.68.3.9")

    def test_scan_returns_none_when_nothing_answers(self):
        with mock.patch.object(self.app, "probe_device", return_value=False):
            self.assertIsNone(self.app.scan_subnet("192.168.2"))

    def test_device_session_bypasses_environment_proxy(self):
        # 装了 Clash 时 HTTP_PROXY 会把 192.168.x.x 也发给代理，设备明明在线也会全部失败
        self.assertFalse(self.module.DEVICE_SESSION.trust_env)
        self.assertEqual(self.module.DEVICE_SESSION.proxies, {})

    def test_connect_success_records_scanned_ip_and_keeps_input(self):
        self.app.ip_var.set("192.168.2")

        self.app.handle_connect_success("192.168.2.42")

        self.assertEqual(self.app.device_ip, "192.168.2.42")
        self.assertEqual(self.app.base_url, "http://192.168.2.42:8080")
        self.assertTrue(self.app.connected)
        # 输入框保留用户填写的网段，下次直接回车即可重新搜索
        self.assertEqual(self.app.ip_var.get(), "192.168.2")
        self.assertEqual(
            self.app.lbl_device_info.cget("text"),
            "已连接: 192.168.2.42"
        )


if __name__ == "__main__":
    unittest.main()
