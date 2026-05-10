import json
import os
import sys
import threading
import tkinter as tk
from ctypes import byref, c_int, sizeof, windll
from datetime import datetime
from tkinter import messagebox

import requests

try:
    import pygame
except Exception as exc:
    pygame = None
    PYGAME_IMPORT_ERROR = exc

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.dirname(os.path.abspath(__file__))

    return os.path.join(base_path, relative_path)


def get_app_dir():
    if getattr(sys, "frozen", False):
        return os.path.dirname(os.path.abspath(sys.executable))
    return os.path.dirname(os.path.abspath(__file__))

# --- 配色方案 (极简风) ---
THEMES = {
    "light": {
        "window_bg": "#F5F5F5",    # 整体背景
        "panel_bg": "#FFFFFF",     # 顶部/底部面板背景
        "fg_primary": "#000000",   # 主要文字
        "fg_secondary": "#666666", # 次要文字
        "input_bg": "#FFFFFF",     # 输入框背景
        "input_fg": "#000000",     # 输入框文字
        "btn_primary": "#0067C0",  # 主按钮 (下一页/连接)
        "btn_primary_fg": "#FFFFFF",
        "btn_secondary": "#E0E0E0",# 次按钮 (上一页)
        "btn_secondary_fg": "#000000",
        "btn_suspend": "#607D8B",  # 休眠按钮
        "btn_suspend_fg": "#FFFFFF",
        "theme_icon_color": "#000000", # 浅色模式下图标为黑色
        "danger": "#D32F2F"        # 红色 (断开)
    },
    "dark": {
        "window_bg": "#1E1E1E",    # 整体背景 (深灰)
        "panel_bg": "#252526",     # 面板背景
        "fg_primary": "#FFFFFF",   # 主要文字 (白)
        "fg_secondary": "#AAAAAA", # 次要文字
        "input_bg": "#333333",     # 输入框背景
        "input_fg": "#FFFFFF",     # 输入框文字
        "btn_primary": "#4CC2FF",  # 主按钮 (亮蓝)
        "btn_primary_fg": "#000000", # 深色模式下主按钮文字用黑
        "btn_secondary": "#3E3E42",# 次按钮
        "btn_secondary_fg": "#FFFFFF",
        "btn_suspend": "#455A64",  # 休眠按钮
        "btn_suspend_fg": "#FFFFFF",
        "theme_icon_color": "#FFFFFF", # 深色模式下图标为白色
        "danger": "#FF5252"        # 红色
    }
}

DEFAULT_GAMEPAD_MAPPING = {
    "next_page": ["DPAD_DOWN", "DPAD_RIGHT"],    # 下一页: 下 / 右
    "previous_page": ["DPAD_UP", "DPAD_LEFT"],   # 上一页: 上 / 左
    "rotate": ["Y", "BTN_3"],
    "refresh": ["X", "BTN_2"],
    "screenshot": ["A", "BTN_0"],
    "suspend": ["B", "BTN_1"],
    "disconnect": ["START", "BTN_7", "BTN_6"]
}

DEFAULT_KEYBOARD_MAPPING = {
    "next_page": ["Next", "Right", "Down"],
    "previous_page": ["Prior", "Left", "Up"],
    "rotate": ["F6"],
    "refresh": ["F5"],
    "screenshot": ["F7"],
    "suspend": ["Escape"]
}

KEYBOARD_KEY_ALIASES = {
    "Esc": "Escape",
    "esc": "Escape",
    "PageUp": "Prior",
    "pageup": "Prior",
    "PgUp": "Prior",
    "pgup": "Prior",
    "PageDown": "Next",
    "pagedown": "Next",
    "PgDn": "Next",
    "pgdn": "Next",
    "ArrowLeft": "Left",
    "arrowleft": "Left",
    "ArrowRight": "Right",
    "arrowright": "Right",
    "ArrowUp": "Up",
    "arrowup": "Up",
    "ArrowDown": "Down",
    "arrowdown": "Down",
    "PrintScreen": "Print",
    "printscreen": "Print",
    "PrtSc": "Print",
    "prtsc": "Print"
}

APP_NAME = "KOReader Page Turner"
APP_ID = "Hugo.KOReaderPageTurner"
APP_DIR = get_app_dir()
CONFIG_FILE = os.path.join(APP_DIR, 'koreader_config.json')
SCREENSHOTS_DIR = os.path.join(APP_DIR, "screenshots")
ICON_PATH = resource_path("logo.ico")
ICON_PNG_PATH = resource_path("logo.png")

UI_BASE_DPI = 96
TK_BASE_SCALING = UI_BASE_DPI / 72
DEFAULT_WINDOW_SIZE_DP = (300, 280)
MIN_WINDOW_SIZE_DP = (280, 240)
ICON_SIZE_BUCKETS = (20, 24, 25, 30, 36, 40, 48)
THEME_ICON_NAMES = {
    "light": "moon",
    "dark": "sun",
}

WM_SETICON = 0x0080
ICON_SMALL = 0
ICON_BIG = 1
IMAGE_ICON = 1
LR_LOADFROMFILE = 0x0010
LR_DEFAULTSIZE = 0x0040
DWMWA_USE_IMMERSIVE_DARK_MODE = 20
DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19
DWMWA_CAPTION_COLOR = 35
DWMWA_TEXT_COLOR = 36
DWMWA_BORDER_COLOR = 34
DWMWA_COLOR_DEFAULT = 0xFFFFFFFF


def set_windows_app_id():
    if sys.platform != "win32":
        return

    try:
        windll.shell32.SetCurrentProcessExplicitAppUserModelID(APP_ID)
    except Exception:
        pass


def set_process_dpi_awareness():
    if sys.platform != "win32":
        return

    try:
        windll.shcore.SetProcessDpiAwareness(1)
    except Exception:
        try:
            windll.user32.SetProcessDPIAware()
        except Exception:
            pass


def scale_ui_value(value, scale):
    return max(1, int(round(float(value) * float(scale))))


def _coerce_positive_int(value):
    try:
        number = int(value)
    except (TypeError, ValueError):
        return None
    return number if number > 0 else None


def get_configured_window_size(config, scale):
    width_dp = _coerce_positive_int(config.get("window_width_dp"))
    height_dp = _coerce_positive_int(config.get("window_height_dp"))
    legacy_width = _coerce_positive_int(config.get("window_width"))
    legacy_height = _coerce_positive_int(config.get("window_height"))

    if width_dp and height_dp:
        width = scale_ui_value(width_dp, scale)
        height = scale_ui_value(height_dp, scale)
    elif legacy_width and legacy_height:
        width = legacy_width
        height = legacy_height
    else:
        width = scale_ui_value(DEFAULT_WINDOW_SIZE_DP[0], scale)
        height = scale_ui_value(DEFAULT_WINDOW_SIZE_DP[1], scale)

    min_width = scale_ui_value(MIN_WINDOW_SIZE_DP[0], scale)
    min_height = scale_ui_value(MIN_WINDOW_SIZE_DP[1], scale)
    return max(width, min_width), max(height, min_height)


def get_window_dpi_scale(window):
    try:
        dpi = float(window.winfo_fpixels("1i"))
    except Exception:
        return 1.0
    if dpi <= 0:
        return 1.0
    return max(1.0, dpi / UI_BASE_DPI)


def configure_tk_scaling(window):
    scale = get_window_dpi_scale(window)
    try:
        window.tk.call("tk", "scaling", TK_BASE_SCALING * scale)
    except Exception:
        pass
    return scale


def pick_icon_size(logical_size, scale, buckets=ICON_SIZE_BUCKETS):
    target = scale_ui_value(logical_size, scale)
    return min(buckets, key=lambda size: (abs(size - target), -size))


def get_icon_asset_relative_path(icon_name, variant, logical_size, scale):
    pixel_size = pick_icon_size(logical_size, scale)
    return os.path.join("assets", "icons", str(pixel_size), f"{icon_name}-{variant}.png").replace("\\", "/")


def get_theme_toggle_icon_name(theme):
    return THEME_ICON_NAMES.get(theme, THEME_ICON_NAMES["light"])


def get_monochrome_icon_variant(theme):
    return "white" if theme == "dark" else "black"


def get_pin_icon_variant(theme, always_on_top):
    if always_on_top:
        return "primary_dark" if theme == "dark" else "primary_light"
    return get_monochrome_icon_variant(theme)


def get_danger_icon_variant(theme):
    return "danger_dark" if theme == "dark" else "danger_light"


def get_native_window_handle(window):
    try:
        window.update_idletasks()
        return windll.user32.GetParent(window.winfo_id()) or window.winfo_id()
    except Exception:
        return 0


def apply_native_window_icon(window):
    if sys.platform != "win32":
        return

    set_windows_app_id()

    try:
        window.iconbitmap(ICON_PATH)
    except Exception:
        pass

    try:
        window._icon_photo = tk.PhotoImage(file=ICON_PNG_PATH)
        window.iconphoto(True, window._icon_photo)
    except Exception:
        pass

    try:
        hwnd = get_native_window_handle(window)
        if not hwnd:
            return
        hicon_small = windll.user32.LoadImageW(0, ICON_PATH, IMAGE_ICON, 16, 16, LR_LOADFROMFILE)
        hicon_big = windll.user32.LoadImageW(0, ICON_PATH, IMAGE_ICON, 32, 32, LR_LOADFROMFILE | LR_DEFAULTSIZE)
        if hicon_small:
            windll.user32.SendMessageW(hwnd, WM_SETICON, ICON_SMALL, hicon_small)
        if hicon_big:
            windll.user32.SendMessageW(hwnd, WM_SETICON, ICON_BIG, hicon_big)
        window._native_icon_handles = (hicon_small, hicon_big)
    except Exception:
        pass


def apply_windows_title_bar_theme(window, is_dark):
    if sys.platform != "win32":
        return

    try:
        hwnd = get_native_window_handle(window)
        if not hwnd:
            return
        dark_value = c_int(1 if is_dark else 0)

        for attr in (DWMWA_USE_IMMERSIVE_DARK_MODE, DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1):
            windll.dwmapi.DwmSetWindowAttribute(hwnd, attr, byref(dark_value), sizeof(dark_value))

        caption = c_int(0x000000 if is_dark else DWMWA_COLOR_DEFAULT)
        text = c_int(0x00FFFFFF if is_dark else DWMWA_COLOR_DEFAULT)
        border = c_int(0x000000 if is_dark else DWMWA_COLOR_DEFAULT)
        windll.dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, byref(caption), sizeof(caption))
        windll.dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, byref(text), sizeof(text))
        windll.dwmapi.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, byref(border), sizeof(border))
    except Exception:
        pass


def normalize_key_name(key_name, aliases=None):
    key = str(key_name).strip()
    if not key:
        return ""
    aliases = aliases or {}
    return aliases.get(key, aliases.get(key.lower(), key))


def normalize_action_mapping(custom_mapping, default_mapping, aliases=None):
    normalized = {}
    custom_mapping = custom_mapping if isinstance(custom_mapping, dict) else {}

    for action, default_keys in default_mapping.items():
        raw_keys = custom_mapping.get(action, default_keys)
        if isinstance(raw_keys, str):
            keys = [raw_keys]
        elif isinstance(raw_keys, list):
            keys = raw_keys
        else:
            keys = default_keys

        normalized_keys = []
        for key in keys:
            normalized_key = normalize_key_name(key, aliases)
            if normalized_key and normalized_key not in normalized_keys:
                normalized_keys.append(normalized_key)
        normalized[action] = normalized_keys

    return normalized


def get_mapped_action(mapping, key_code, aliases=None):
    normalized_key = normalize_key_name(key_code, aliases)
    for action, keys in mapping.items():
        if normalized_key in keys:
            return action
    return None


def build_koreader_url(base_url, endpoint):
    return f"{base_url.rstrip('/')}/{endpoint.lstrip('/')}"


def ensure_success_status(response):
    response.raise_for_status()
    return response


class ToolTip:
    def __init__(self, widget, text):
        self.widget = widget
        self.text = text
        self.tip_window = None
        widget.bind("<Enter>", self.show)
        widget.bind("<Leave>", self.hide)

    def show(self, _event=None):
        if self.tip_window or not self.text:
            return
        x = self.widget.winfo_rootx() + 12
        y = self.widget.winfo_rooty() + self.widget.winfo_height() + 6
        self.tip_window = tk.Toplevel(self.widget)
        self.tip_window.wm_overrideredirect(True)
        self.tip_window.wm_geometry(f"+{x}+{y}")
        label = tk.Label(
            self.tip_window,
            text=self.text,
            bg="#222222",
            fg="#FFFFFF",
            padx=8,
            pady=4,
            font=("Microsoft YaHei UI", 9)
        )
        label.pack()

    def hide(self, _event=None):
        if self.tip_window:
            self.tip_window.destroy()
            self.tip_window = None

class KOReaderRemoteApp:
    def __init__(self, root):
        self.root = root
        self.root.title(APP_NAME)
        self.ui_scale = configure_tk_scaling(self.root)
        self.icon_images = {}
        
        # === 加载自定义图标 ===
        apply_native_window_icon(self.root)
        # 核心变量
        self.connected = False
        self.base_url = ""
        self.ip_var = tk.StringVar()
        self.current_theme = "light"
        self.always_on_top = False  # 置顶状态
        self.rotation_state = 0 # 0: Portrait, 1: Landscape
        self.config = {}
        
        # 加载配置（在设置窗口大小之前）
        self.load_config()
        
        # 设置窗口大小（优先使用不随 DPI 漂移的逻辑尺寸）
        width, height = get_configured_window_size(self.config, self.ui_scale)
        self.root.geometry(f"{width}x{height}")
        self.center_window(width, height)
        self.root.resizable(True, True)  # 允许调整大小
        self.root.minsize(self.px(MIN_WINDOW_SIZE_DP[0]), self.px(MIN_WINDOW_SIZE_DP[1]))
        
        # 应用置顶状态
        # 应用置顶状态
        self.root.after(0, self.refresh_native_window_chrome)
        self.root.after(200, self.refresh_native_window_chrome)
        self.root.attributes('-topmost', self.always_on_top)
        
        # 构建界面
        self.create_widgets()
        
        # 应用主题
        self.apply_theme()
        
        # 窗口大小变化时保存配置
        self.root.bind('<Configure>', self.on_window_configure)
        self.root.protocol("WM_DELETE_WINDOW", self.on_close)
        
        # === 手柄初始化 ===
        self.gamepad_running = pygame is not None
        if self.gamepad_running:
            self.gamepad_thread = threading.Thread(target=self.gamepad_poll_loop, daemon=True)
            self.gamepad_thread.start()
        else:
            print(f"pygame unavailable, gamepad disabled: {PYGAME_IMPORT_ERROR}")
        
        # === 键盘绑定 ===
        self.root.bind('<KeyPress>', self.on_key_press)
        self.root.bind('<MouseWheel>', self.on_mouse_wheel)

    def px(self, value):
        return scale_ui_value(value, self.ui_scale)

    def get_icon_image(self, icon_name, variant, logical_size=20):
        key = (icon_name, variant, logical_size)
        if key in self.icon_images:
            return self.icon_images[key]

        relative_path = get_icon_asset_relative_path(icon_name, variant, logical_size, self.ui_scale)
        path = resource_path(relative_path)
        try:
            image = tk.PhotoImage(file=path)
        except Exception:
            image = None
        self.icon_images[key] = image
        return image

    def set_button_icon(self, button, icon_name, variant, text="", fallback_text="", logical_size=20, compound=tk.LEFT):
        image = self.get_icon_image(icon_name, variant, logical_size)
        if image:
            button.configure(image=image, text=text, compound=compound)
        else:
            button.configure(image="", text=fallback_text or text)

    def center_window(self, width, height):
        screen_width = self.root.winfo_screenwidth()
        screen_height = self.root.winfo_screenheight()
        x = (screen_width - width) // 2
        y = (screen_height - height) // 2
        self.root.geometry(f'{width}x{height}+{x}+{y}')

    def refresh_native_window_chrome(self):
        apply_native_window_icon(self.root)
        apply_windows_title_bar_theme(self.root, self.current_theme == "dark")

    def create_widgets(self):
        # === 1. 顶部标题栏 (极简版) ===
        title_bar_height = self.px(32)
        title_button_width = self.px(34)
        title_button_height = max(1, title_bar_height - self.px(2))

        self.title_bar = tk.Frame(self.root, height=title_bar_height)
        self.title_bar.pack(fill=tk.X, side=tk.TOP)
        self.title_bar.pack_propagate(False)
        
        # 标题文字已移除
        
        # === 手柄状态指示灯 ===
        # 在最左侧添加一个指示灯
        status_light_size = self.px(14)
        status_light_inset = self.px(3)
        self.canvas_gamepad_status = tk.Canvas(self.title_bar, width=status_light_size, height=status_light_size,
                                               bg=THEMES[self.current_theme]['panel_bg'], 
                                               highlightthickness=0)
        self.canvas_gamepad_status.pack(side=tk.LEFT, padx=(self.px(5), self.px(1)))
        # 初始绘制红色圆点
        self.status_light = self.canvas_gamepad_status.create_oval(
            status_light_inset,
            status_light_inset,
            status_light_size - status_light_inset,
            status_light_size - status_light_inset,
            fill="#FF5252",
            outline=""
        )
        self.update_gamepad_status(False) # 初始状态: 未连接

        title_button_options = {
            "relief": tk.FLAT,
            "bd": 0,
            "cursor": "hand2",
            "width": title_button_width,
            "height": title_button_height,
            "padx": 0,
            "pady": 0,
            "highlightthickness": 0,
            "activebackground": None,
        }
        
        # 主题切换按钮
        self.btn_theme = tk.Button(self.title_bar, text="", font=("Segoe UI Symbol", 12),
                                   command=self.toggle_theme, **title_button_options)
        self.btn_theme.pack(side=tk.RIGHT, fill=tk.Y)
        
        # 图钉按钮 (置顶功能，在主题按钮左侧)
        self.btn_pin = tk.Button(self.title_bar, text="", font=("Segoe UI Symbol", 10),
                                 command=self.toggle_always_on_top, **title_button_options)
        self.btn_pin.pack(side=tk.RIGHT, fill=tk.Y, padx=self.px(1))

        # === 新增功能按钮 (最左侧) ===
        # 字体选用 Segoe UI Symbol 或其他通用字体以更好显示特殊符号
        btn_font = ("Segoe UI Symbol", 11)
        
        # 旋转屏幕 (Rotate) - 状态切换
        self.btn_rotate = tk.Button(self.title_bar, text="", font=btn_font,
                                    command=self.rotate_device, **title_button_options)
        self.btn_rotate.pack(side=tk.LEFT, fill=tk.Y, padx=self.px(1))
        ToolTip(self.btn_rotate, "旋转 (F6)")

        # 刷新屏幕 (Full Refresh)
        self.btn_refresh = tk.Button(self.title_bar, text="", font=btn_font,
                                     command=self.full_refresh, **title_button_options)
        self.btn_refresh.pack(side=tk.LEFT, fill=tk.Y, padx=self.px(1))
        ToolTip(self.btn_refresh, "全刷 (F5)")

        # 截图 (Screenshot)
        self.btn_screenshot = tk.Button(self.title_bar, text="", font=btn_font,
                                        command=self.take_screenshot, **title_button_options)
        self.btn_screenshot.pack(side=tk.LEFT, fill=tk.Y, padx=self.px(1))
        ToolTip(self.btn_screenshot, "截图 (F7)")

        # === 2. 底部状态栏 ===
        self.status_bar = tk.Frame(self.root, height=self.px(25))
        self.status_bar.pack(fill=tk.X, side=tk.BOTTOM)
        self.status_bar.pack_propagate(False)
        
        self.lbl_status = tk.Label(self.status_bar, text="准备就绪", font=("Segoe UI", 8))
        self.lbl_status.pack(side=tk.LEFT, padx=self.px(10))

        # === 3. 主内容区域 ===
        self.main_container = tk.Frame(self.root)
        self.main_container.pack(fill=tk.BOTH, expand=True, padx=self.px(15), pady=self.px(10))
        
        # --- 页面A: 登录页 ---
        self.page_login = tk.Frame(self.main_container)
        
        tk.Label(self.page_login, text="连接设备", font=("Microsoft YaHei UI", 16, "bold")).pack(
            pady=(self.px(15), self.px(15))
        )
        
        # IP 输入框容器
        self.frame_ip = tk.Frame(self.page_login, padx=self.px(1), pady=self.px(1))
        self.frame_ip_inner = tk.Frame(self.frame_ip, padx=self.px(10), pady=self.px(8))
        
        self.frame_ip.pack(fill=tk.X, pady=(0, self.px(15)))
        self.frame_ip_inner.pack(fill=tk.BOTH)
        
        tk.Label(self.frame_ip_inner, text="IP 地址:", font=("Segoe UI", 9)).pack(anchor="w")
        self.entry_ip = tk.Entry(self.frame_ip_inner, textvariable=self.ip_var, font=("Segoe UI", 11), relief=tk.FLAT)
        self.entry_ip.pack(fill=tk.X, pady=(self.px(2), 0))
        self.entry_ip.bind('<Return>', lambda e: self.on_connect_click())
        
        # 连接按钮
        self.btn_connect = tk.Button(self.page_login, text="立即连接", font=("Microsoft YaHei UI", 11),
                                     relief=tk.FLAT, cursor="hand2",
                                     command=self.on_connect_click)
        self.btn_connect.pack(fill=tk.X, ipady=self.px(6))

        # --- 页面B: 控制页 ---
        self.page_control = tk.Frame(self.main_container)
        
        # 设备信息行
        self.frame_info = tk.Frame(self.page_control)
        self.frame_info.pack(fill=tk.X, pady=(0, self.px(5)))
        
        self.lbl_device_info = tk.Label(self.frame_info, text="已连接", font=("Microsoft YaHei UI", 9, "bold"))
        self.lbl_device_info.pack(side=tk.LEFT)
        
        self.btn_disconnect = tk.Button(self.frame_info, text="断开", font=("Microsoft YaHei UI", 9),
                                        relief=tk.FLAT, cursor="hand2", bd=0,
                                        command=self.disconnect)
        self.btn_disconnect.pack(side=tk.RIGHT)
        
        # 翻页控制区 (Grid布局)
        self.frame_actions = tk.Frame(self.page_control)
        self.frame_actions.pack(fill=tk.BOTH, expand=True)
        self.frame_actions.columnconfigure(0, weight=1)
        self.frame_actions.columnconfigure(1, weight=1)
        self.frame_actions.rowconfigure(0, weight=1)
        
        # 上一页
        self.btn_prev = tk.Button(self.frame_actions, text="< 上一页\n(↑)", font=("Microsoft YaHei UI", 12),
                                  relief=tk.FLAT, cursor="hand2",
                                  command=self.previous_page)
        self.btn_prev.grid(row=0, column=0, sticky="nsew", padx=(0, self.px(4)), pady=(0, self.px(10)))
        
        # 下一页
        self.btn_next = tk.Button(self.frame_actions, text="下一页 >\n(↓)", font=("Microsoft YaHei UI", 14, "bold"),
                                  relief=tk.FLAT, cursor="hand2",
                                  command=self.next_page)
        self.btn_next.grid(row=0, column=1, sticky="nsew", padx=(self.px(4), 0), pady=(0, self.px(10)))
        
        # 休眠按钮
        self.btn_suspend = tk.Button(self.page_control, text="😴 设备休眠 (Esc)", font=("Microsoft YaHei UI", 10),
                                     relief=tk.FLAT, cursor="hand2",
                                     command=self.suspend_device)
        self.btn_suspend.pack(fill=tk.X, ipady=self.px(8))

        self.show_login()

    def apply_theme(self):
        if self.current_theme not in THEMES: self.current_theme = "light"
        t = THEMES[self.current_theme]
        
        self.refresh_native_window_chrome()
        self.root.after_idle(self.refresh_native_window_chrome)
        # 1. 背景色
        for w in [self.root, self.main_container, self.page_login, self.page_control, 
                  self.frame_actions, self.frame_info]:
            w.configure(bg=t['window_bg'])
        
        # 2. 面板颜色
        self.title_bar.configure(bg=t['panel_bg'])
        self.status_bar.configure(bg=t['panel_bg'])
        self.lbl_status.configure(bg=t['panel_bg'], fg=t['fg_secondary'])
        
        # 3. 主题按钮
        self.btn_theme.configure(bg=t['panel_bg'], fg=t['theme_icon_color'], 
                                 activebackground=t['panel_bg'], activeforeground=t['theme_icon_color'])
        icon_variant = get_monochrome_icon_variant(self.current_theme)
        self.set_button_icon(
            self.btn_theme,
            get_theme_toggle_icon_name(self.current_theme),
            icon_variant,
            fallback_text="☾" if self.current_theme == "light" else "☀",
            compound=tk.CENTER
        )

        # 3.1 状态灯背景
        self.canvas_gamepad_status.configure(bg=t['panel_bg'])
        
        # 4. 图钉按钮
        pin_color = t['btn_primary'] if self.always_on_top else t['theme_icon_color']
        self.btn_pin.configure(bg=t['panel_bg'], fg=pin_color,
                               activebackground=t['panel_bg'], activeforeground=pin_color)
        self.set_button_icon(
            self.btn_pin,
            "pin",
            get_pin_icon_variant(self.current_theme, self.always_on_top),
            fallback_text="📌",
            compound=tk.CENTER
        )
        
        # 5. 新增功能按钮主题
        for btn in [self.btn_rotate, self.btn_refresh, self.btn_screenshot]:
            btn.configure(bg=t['panel_bg'], fg=t['theme_icon_color'],
                          activebackground=t['panel_bg'], activeforeground=t['theme_icon_color'])
        self.set_button_icon(self.btn_rotate, "rotate-cw", icon_variant, fallback_text="⟳", compound=tk.CENTER)
        self.set_button_icon(self.btn_refresh, "refresh-cw", icon_variant, fallback_text="↻", compound=tk.CENTER)
        self.set_button_icon(self.btn_screenshot, "camera", icon_variant, fallback_text="📷", compound=tk.CENTER)
        
        # 4. 登录页
        self.page_login.winfo_children()[0].configure(bg=t['window_bg'], fg=t['fg_primary'])
        self.frame_ip.configure(bg=t['fg_secondary'])
        self.frame_ip_inner.configure(bg=t['input_bg'])
        self.frame_ip_inner.winfo_children()[0].configure(bg=t['input_bg'], fg=t['fg_secondary'])
        self.entry_ip.configure(bg=t['input_bg'], fg=t['input_fg'], insertbackground=t['fg_primary'])
        
        self.btn_connect.configure(bg=t['btn_primary'], fg=t['btn_primary_fg'],
                                   activebackground=t['btn_primary'], activeforeground=t['btn_primary_fg'])
        
        # 5. 控制页
        self.lbl_device_info.configure(bg=t['window_bg'], fg=t['fg_primary'])
        self.btn_disconnect.configure(bg=t['window_bg'], fg=t['danger'],
                                      activebackground=t['window_bg'], activeforeground=t['danger'])
        self.set_button_icon(
            self.btn_disconnect,
            "unplug",
            get_danger_icon_variant(self.current_theme),
            text="断开",
            fallback_text="断开",
            logical_size=20,
            compound=tk.LEFT
        )
        
        self.btn_prev.configure(bg=t['btn_secondary'], fg=t['btn_secondary_fg'],
                                activebackground=t['btn_secondary'], activeforeground=t['btn_secondary_fg'])
        self.btn_next.configure(bg=t['btn_primary'], fg=t['btn_primary_fg'],
                                activebackground=t['btn_primary'], activeforeground=t['btn_primary_fg'])
        self.btn_suspend.configure(bg=t['btn_suspend'], fg=t['btn_suspend_fg'],
                                   activebackground=t['btn_suspend'], activeforeground=t['btn_suspend_fg'])
        secondary_icon_variant = "white" if t['btn_secondary_fg'] == "#FFFFFF" else "black"
        primary_icon_variant = "white" if t['btn_primary_fg'] == "#FFFFFF" else "black"
        self.set_button_icon(
            self.btn_prev,
            "chevron-left",
            secondary_icon_variant,
            text="上一页\n(↑)",
            fallback_text="< 上一页\n(↑)",
            logical_size=24,
            compound=tk.LEFT
        )
        self.set_button_icon(
            self.btn_next,
            "chevron-right",
            primary_icon_variant,
            text="下一页\n(↓)",
            fallback_text="下一页 >\n(↓)",
            logical_size=24,
            compound=tk.RIGHT
        )
        self.set_button_icon(
            self.btn_suspend,
            "power",
            "white",
            text="设备休眠 (Esc)",
            fallback_text="😴 设备休眠 (Esc)",
            logical_size=20,
            compound=tk.LEFT
        )

    def toggle_theme(self):
        self.current_theme = "dark" if self.current_theme == "light" else "light"
        self.apply_theme()
        self.save_config()

    def load_config(self):
        self.config = {}
        if os.path.exists(CONFIG_FILE):
            try:
                with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
                    self.config = json.load(f)
                    self.ip_var.set(self.config.get('last_ip', ''))
                    self.current_theme = self.config.get('theme', 'light')
                    self.always_on_top = self.config.get('always_on_top', False)
                    self.gamepad_mapping = normalize_action_mapping(
                        self.config.get('gamepad_mapping'),
                        DEFAULT_GAMEPAD_MAPPING
                    )
                    self.keyboard_mapping = normalize_action_mapping(
                        self.config.get('keyboard_mapping'),
                        DEFAULT_KEYBOARD_MAPPING,
                        KEYBOARD_KEY_ALIASES
                    )
                    print(f"配置文件加载成功: {CONFIG_FILE}")
            except Exception as e: 
                print(f"加载配置文件失败: {e}")
                # 如果是 JSON 格式错误，提示用户
                if isinstance(e, json.JSONDecodeError):
                    messagebox.showerror("配置错误", f"配置文件格式错误，无法加载。\n请检查 JSON 语法。\n\n错误信息: {e}")
                
                self.current_theme = "light"
                self.always_on_top = False
                self.gamepad_mapping = normalize_action_mapping(None, DEFAULT_GAMEPAD_MAPPING)
                self.keyboard_mapping = normalize_action_mapping(
                    None,
                    DEFAULT_KEYBOARD_MAPPING,
                    KEYBOARD_KEY_ALIASES
                )
        else: 
            self.current_theme = "light"
            self.always_on_top = False
            self.gamepad_mapping = normalize_action_mapping(None, DEFAULT_GAMEPAD_MAPPING)
            self.keyboard_mapping = normalize_action_mapping(
                None,
                DEFAULT_KEYBOARD_MAPPING,
                KEYBOARD_KEY_ALIASES
            )

    def save_config(self):
        try:
            self.config['last_ip'] = self.ip_var.get().strip()
            self.config['theme'] = self.current_theme
            self.config['always_on_top'] = self.always_on_top
            self.config['gamepad_mapping'] = getattr(self, 'gamepad_mapping', DEFAULT_GAMEPAD_MAPPING)
            self.config['keyboard_mapping'] = getattr(self, 'keyboard_mapping', DEFAULT_KEYBOARD_MAPPING)
            self.config['window_width'] = self.root.winfo_width()
            self.config['window_height'] = self.root.winfo_height()
            self.config['window_width_dp'] = max(1, int(round(self.root.winfo_width() / self.ui_scale)))
            self.config['window_height_dp'] = max(1, int(round(self.root.winfo_height() / self.ui_scale)))
            with open(CONFIG_FILE, 'w', encoding='utf-8') as f:
                json.dump(self.config, f, indent=4, ensure_ascii=False)
        except Exception:
            pass
    
    def toggle_always_on_top(self):
        """切换窗口置顶状态"""
        self.always_on_top = not self.always_on_top
        self.root.attributes('-topmost', self.always_on_top)
        self.apply_theme()  # 更新图钉按钮颜色
        self.save_config()
        self.show_log("已置顶" if self.always_on_top else "取消置顶")
    
    def on_window_configure(self, event):
        """窗口大小变化时保存配置"""
        # 仅在主窗口大小变化时保存
        if event.widget == self.root:
            # 使用 after 防止频繁保存
            if hasattr(self, '_save_timer'):
                self.root.after_cancel(self._save_timer)
            self._save_timer = self.root.after(500, self.save_config)

    def on_connect_click(self):
        ip = self.ip_var.get().strip()
        if not ip:
            messagebox.showwarning("提示", "请输入 IP 地址")
            return
        self.btn_connect.config(state=tk.DISABLED, text="连接中...")
        self.base_url = f"http://{ip}:8080"
        threading.Thread(target=self.thread_check_connection, daemon=True).start()

    def thread_check_connection(self):
        try:
            resp = requests.get(f"{self.base_url}/koreader/event/GotoViewRel/0", timeout=3)
            if resp.status_code == 200:
                self.root.after(0, self.handle_connect_success)
            else:
                raise Exception("Error")
        except:
            self.root.after(0, lambda: self.handle_connect_fail(None))

    def handle_connect_success(self):
        self.connected = True
        self.save_config()
        self.btn_connect.config(state=tk.NORMAL, text="立即连接")
        self.show_control()
        self.show_log("连接成功")

    def handle_connect_fail(self, _):
        self.btn_connect.config(state=tk.NORMAL, text="立即连接")
        messagebox.showerror("连接失败", "无法连接。请检查IP或确认HTTP Server已开启。")

    def show_login(self):
        self.page_control.pack_forget()
        self.page_login.pack(fill=tk.BOTH, expand=True)

    def show_control(self):
        self.page_login.pack_forget()
        self.page_control.pack(fill=tk.BOTH, expand=True)
        self.lbl_device_info.config(text=f"已连接: {self.ip_var.get()}")

    def disconnect(self):
        self.connected = False
        self.show_login()

    def ensure_connected(self):
        if self.connected:
            return True
        self.show_log("请先连接设备", True)
        return False

    def send_cmd(self, endpoint, log_text):
        if not self.ensure_connected():
            return
        def _req():
            try:
                resp = requests.get(build_koreader_url(self.base_url, endpoint), timeout=2)
                ensure_success_status(resp)
                self.root.after(0, lambda: self.show_log(log_text))
            except:
                self.root.after(0, lambda: self.show_log("失败", True))
        threading.Thread(target=_req, daemon=True).start()

    def trigger_action(self, action):
        if action == "next_page":
            self.next_page()
        elif action == "previous_page":
            self.previous_page()
        elif action == "rotate":
            self.rotate_device()
        elif action == "refresh":
            self.full_refresh()
        elif action == "screenshot":
            self.take_screenshot()
        elif action == "suspend":
            self.suspend_device()
        elif action == "disconnect":
            self.disconnect()

    def on_key_press(self, event):
        action = get_mapped_action(
            self.keyboard_mapping,
            event.keysym,
            KEYBOARD_KEY_ALIASES
        )
        if not action:
            return None

        self.trigger_action(action)
        return "break"

    def previous_page(self): self.send_cmd("/koreader/event/GotoViewRel/-1", "上一页")
    def next_page(self): self.send_cmd("/koreader/event/GotoViewRel/1", "下一页")
    def suspend_device(self): self.send_cmd("/koreader/event/RequestSuspend", "已发送休眠")
    
    # === 新增功能 ===
    # === 新增功能 ===
    def rotate_device(self):
        # 切换旋转状态: 0 -> 1 -> 0
        new_state = 1 if self.rotation_state == 0 else 0
        cmd = f"/koreader/event/SetRotationMode/{new_state}"
        self.send_cmd(cmd, "旋转中...")
        self.rotation_state = new_state

    def full_refresh(self):
        # 尝试发送全刷命令
        self.send_cmd("/koreader/event/FullRefresh", "请求刷新")

    def take_screenshot(self):
        if not self.ensure_connected():
            return

        url = build_koreader_url(self.base_url, "/koreader/device/screen/bb")
        
        def _shot():
            try:
                self.root.after(0, lambda: self.show_log("正在截图(等待渲染)..."))
                
                # 设置请求头，模拟浏览器
                headers = {
                    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                }

                # 重试机制 (最多3次)
                session = requests.Session()
                adapter = requests.adapters.HTTPAdapter(max_retries=3)
                session.mount('http://', adapter)
                
                # 增加超时时间到90秒，因为墨水屏渲染可能很慢
                # Stream下载以避免 IncompleteRead
                with session.get(url, headers=headers, stream=True, timeout=(5, 90)) as resp:
                    ensure_success_status(resp)
                    
                    # 生成文件名
                    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                    filename = f"screenshot_{timestamp}.png"
                    
                    os.makedirs(SCREENSHOTS_DIR, exist_ok=True)
                    filepath = os.path.join(SCREENSHOTS_DIR, filename)

                    # 使用 iter_content 分块写入，更稳健
                    with open(filepath, "wb") as f:
                        for chunk in resp.iter_content(chunk_size=8192):
                            if chunk:
                                f.write(chunk)
                    
                    self.root.after(0, lambda: self.show_log(f"已保存: screenshots/{filename}"))

            except requests.exceptions.ChunkedEncodingError:
                self.root.after(0, lambda: self.show_log(f"截图失败: 数据流中断", True))
                self.root.after(0, lambda: messagebox.showerror("截图失败", "数据传输中断(IncompleteRead)。\n可能是设备生成图片太慢或网络不稳定，请重试。"))
            except Exception as e:
                err_msg = str(e)
                if "IncompleteRead" in err_msg:
                    err_msg = "传输中断，请重试"
                elif "timed out" in err_msg:
                    err_msg = "请求超时(设备响应太慢)"
                
                self.root.after(0, lambda: self.show_log(f"截图失败", True))
                self.root.after(0, lambda: messagebox.showerror("截图失败", f"无法获取截图:\n{err_msg}"))

        threading.Thread(target=_shot, daemon=True).start()

    def on_mouse_wheel(self, event):
        if not self.connected: return
        if event.delta > 0: self.previous_page()
        else: self.next_page()

    def show_log(self, text, is_error=False):
        t = THEMES[self.current_theme]
        color = t['danger'] if is_error else t['fg_secondary']
        time = datetime.now().strftime("%H:%M:%S")
        self.lbl_status.config(text=f"{time} - {text}", fg=color)

    def update_gamepad_status(self, connected):
        """更新手柄状态指示灯"""
        color = "#4CAF50" if connected else "#FF5252" # 绿 / 红
        try:
            self.canvas_gamepad_status.itemconfig(self.status_light, fill=color)
        except: pass

    def gamepad_poll_loop(self):
        """手柄轮询线程 (稳定版)"""
        if pygame is None:
            return
        try:
            pygame.init()
            pygame.joystick.init()
            print("Pygame 模块初始化完成")
        except Exception as e:
            print(f"Pygame 初始化失败: {e}")
            return

        # 初始检测
        if pygame.joystick.get_count() > 0:
            try:
                js = pygame.joystick.Joystick(0)
                js.init()
                self.root.after(0, lambda: self.update_gamepad_status(True))
                print(f"初始连接手柄: {js.get_name()}")
            except: pass
        
        # 记录上一次的 hat 状态，防止重复触发
        last_hat_state = (0, 0)
        
        while self.gamepad_running:
            try:
                # 处理 Pygame 事件队列
                for event in pygame.event.get():
                    if event.type == pygame.JOYDEVICEADDED:
                        # 新手柄插入
                        try:
                            js = pygame.joystick.Joystick(event.device_index)
                            js.init()
                            self.root.after(0, lambda: self.update_gamepad_status(True))
                            print(f"手柄已连接: {js.get_name()}")
                        except Exception as e:
                            print(f"手柄初始化失败: {e}")
                    
                    elif event.type == pygame.JOYDEVICEREMOVED:
                        # 手柄拔出
                        # 检查是否还有其他手柄
                        if pygame.joystick.get_count() == 0:
                            self.root.after(0, lambda: self.update_gamepad_status(False))
                            print("手柄已断开")

                    elif event.type == pygame.JOYBUTTONDOWN:
                        # 收集该按钮对应的所有键码
                        keys_pressed = ["BTN_" + str(event.button)]
                        
                        xbox_map = {0:"A", 1:"B", 2:"X", 3:"Y", 4:"LB", 5:"RB", 6:"BACK", 7:"START"}
                        if event.button in xbox_map:
                             keys_pressed.append(xbox_map[event.button])
                        
                        print(f"手柄按键按下: ID={event.button}, Keys={keys_pressed}")

                        # 查找映射功能 (防止重复触发)
                        triggered_action = None
                        for action, mapped_keys in self.gamepad_mapping.items():
                            # 检查是否命中了该 action 的任意一个绑定键
                            if any(k in mapped_keys for k in keys_pressed):
                                triggered_action = action
                                break
                        
                        if triggered_action:
                            print(f"-> 触发功能: {triggered_action}")
                            self.root.after(0, lambda action=triggered_action: self.trigger_action(action))
                        else:
                            print("-> 未绑定功能")

                    elif event.type == pygame.JOYHATMOTION:
                        hat_val = event.value
                        if hat_val != last_hat_state:
                            if hat_val == (0, 1): self.handle_gamepad_input("DPAD_UP")
                            elif hat_val == (0, -1): self.handle_gamepad_input("DPAD_DOWN")
                            elif hat_val == (-1, 0): self.handle_gamepad_input("DPAD_LEFT")
                            elif hat_val == (1, 0): self.handle_gamepad_input("DPAD_RIGHT")
                            last_hat_state = hat_val
                
            except Exception as e:
                print(f"Gamepad loop error: {e}")
            
            pygame.time.wait(10) # 10ms 轮询间隔

    def handle_gamepad_input(self, key_code):
        """处理映射后的手柄按键"""
        # 遍历映射配置，找到对应的功能
        cmd = None
        for action, keys in self.gamepad_mapping.items():
            if key_code in keys:
                cmd = action
                break
        
        if cmd:
            # 在主线程执行对应操作
            self.root.after(0, lambda action=cmd: self.trigger_action(action))

    def on_close(self):
        self.gamepad_running = False
        self.root.destroy()
        sys.exit(0)

def main():
    set_windows_app_id()
    set_process_dpi_awareness()
    root = tk.Tk()
    app = KOReaderRemoteApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()
