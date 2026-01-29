import tkinter as tk
from tkinter import messagebox, ttk
import requests
import threading
import json
import os
import sys
from datetime import datetime

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)

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

CONFIG_FILE = 'koreader_config.json'

class KOReaderRemoteApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Hugoの翻页助手")
        
        # === 加载自定义图标 ===
        try:
            self.root.iconbitmap(resource_path("icon.ico"))
        except:
            pass

        # 核心变量
        self.connected = False
        self.base_url = ""
        self.ip_var = tk.StringVar()
        self.current_theme = "light"
        self.always_on_top = False  # 置顶状态
        self.config = {}
        
        # 加载配置（在设置窗口大小之前）
        self.load_config()
        
        # 设置窗口大小（从配置读取或使用默认值）
        default_width = 300
        default_height = 280
        width = self.config.get('window_width', default_width)
        height = self.config.get('window_height', default_height)
        self.root.geometry(f"{width}x{height}")
        self.center_window(width, height)
        self.root.resizable(True, True)  # 允许调整大小
        self.root.minsize(250, 220)  # 设置最小尺寸
        
        # 应用置顶状态
        self.root.attributes('-topmost', self.always_on_top)
        
        # 构建界面
        self.create_widgets()
        
        # 应用主题
        self.apply_theme()
        
        # 窗口大小变化时保存配置
        self.root.bind('<Configure>', self.on_window_configure)
        
        # === 键盘绑定 ===
        self.root.bind('<Prior>', lambda e: self.previous_page())
        self.root.bind('<Next>', lambda e: self.next_page())
        self.root.bind('<Left>', lambda e: self.previous_page())
        self.root.bind('<Right>', lambda e: self.next_page())
        self.root.bind('<Up>', lambda e: self.previous_page())
        self.root.bind('<Down>', lambda e: self.next_page())
        self.root.bind('<MouseWheel>', self.on_mouse_wheel)
        self.root.bind('<Escape>', lambda e: self.suspend_device())

    def center_window(self, width, height):
        screen_width = self.root.winfo_screenwidth()
        screen_height = self.root.winfo_screenheight()
        x = (screen_width - width) // 2
        y = (screen_height - height) // 2
        self.root.geometry(f'{width}x{height}+{x}+{y}')

    def create_widgets(self):
        # === 1. 顶部标题栏 (极简版) ===
        # 高度减小到 32px，仅容纳按钮
        self.title_bar = tk.Frame(self.root, height=32)
        self.title_bar.pack(fill=tk.X, side=tk.TOP)
        self.title_bar.pack_propagate(False)
        
        # 标题文字已移除
        
        # 主题切换按钮
        self.btn_theme = tk.Button(self.title_bar, text="☾", font=("Arial", 14),
                                   relief=tk.FLAT, bd=0, cursor="hand2", width=4,
                                   activebackground=None,
                                   command=self.toggle_theme)
        self.btn_theme.pack(side=tk.RIGHT, fill=tk.Y)
        
        # 图钉按钮 (置顶功能，在主题按钮左侧)
        self.btn_pin = tk.Button(self.title_bar, text="📌", font=("Arial", 12),
                                 relief=tk.FLAT, bd=0, cursor="hand2", width=4,
                                 activebackground=None,
                                 command=self.toggle_always_on_top)
        self.btn_pin.pack(side=tk.RIGHT, fill=tk.Y)

        # === 2. 底部状态栏 ===
        self.status_bar = tk.Frame(self.root, height=25)
        self.status_bar.pack(fill=tk.X, side=tk.BOTTOM)
        
        self.lbl_status = tk.Label(self.status_bar, text="准备就绪", font=("Segoe UI", 8))
        self.lbl_status.pack(side=tk.LEFT, padx=10)

        # === 3. 主内容区域 ===
        self.main_container = tk.Frame(self.root)
        self.main_container.pack(fill=tk.BOTH, expand=True, padx=15, pady=10)
        
        # --- 页面A: 登录页 ---
        self.page_login = tk.Frame(self.main_container)
        
        tk.Label(self.page_login, text="连接设备", font=("Microsoft YaHei UI", 16, "bold")).pack(pady=(15, 15))
        
        # IP 输入框容器
        self.frame_ip = tk.Frame(self.page_login, padx=1, pady=1)
        self.frame_ip_inner = tk.Frame(self.frame_ip, padx=10, pady=8)
        
        self.frame_ip.pack(fill=tk.X, pady=(0, 15))
        self.frame_ip_inner.pack(fill=tk.BOTH)
        
        tk.Label(self.frame_ip_inner, text="IP 地址:", font=("Segoe UI", 9)).pack(anchor="w")
        self.entry_ip = tk.Entry(self.frame_ip_inner, textvariable=self.ip_var, font=("Segoe UI", 11), relief=tk.FLAT)
        self.entry_ip.pack(fill=tk.X, pady=(2, 0))
        self.entry_ip.bind('<Return>', lambda e: self.on_connect_click())
        
        # 连接按钮
        self.btn_connect = tk.Button(self.page_login, text="立即连接", font=("Microsoft YaHei UI", 11),
                                     relief=tk.FLAT, cursor="hand2",
                                     command=self.on_connect_click)
        self.btn_connect.pack(fill=tk.X, ipady=6)

        # --- 页面B: 控制页 ---
        self.page_control = tk.Frame(self.main_container)
        
        # 设备信息行
        self.frame_info = tk.Frame(self.page_control)
        self.frame_info.pack(fill=tk.X, pady=(0, 5))
        
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
        self.btn_prev.grid(row=0, column=0, sticky="nsew", padx=(0, 4), pady=(0, 10))
        
        # 下一页
        self.btn_next = tk.Button(self.frame_actions, text="下一页 >\n(↓)", font=("Microsoft YaHei UI", 14, "bold"),
                                  relief=tk.FLAT, cursor="hand2",
                                  command=self.next_page)
        self.btn_next.grid(row=0, column=1, sticky="nsew", padx=(4, 0), pady=(0, 10))
        
        # 休眠按钮
        self.btn_suspend = tk.Button(self.page_control, text="😴 设备休眠 (Esc)", font=("Microsoft YaHei UI", 10),
                                     relief=tk.FLAT, cursor="hand2",
                                     command=self.suspend_device)
        self.btn_suspend.pack(fill=tk.X, ipady=8)

        self.show_login()

    def apply_theme(self):
        if self.current_theme not in THEMES: self.current_theme = "light"
        t = THEMES[self.current_theme]
        
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
        self.btn_theme.config(text="☾" if self.current_theme == "light" else "☀")
        
        # 4. 图钉按钮
        pin_color = t['btn_primary'] if self.always_on_top else t['theme_icon_color']
        self.btn_pin.configure(bg=t['panel_bg'], fg=pin_color,
                               activebackground=t['panel_bg'], activeforeground=pin_color)
        
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
        
        self.btn_prev.configure(bg=t['btn_secondary'], fg=t['btn_secondary_fg'],
                                activebackground=t['btn_secondary'], activeforeground=t['btn_secondary_fg'])
        self.btn_next.configure(bg=t['btn_primary'], fg=t['btn_primary_fg'],
                                activebackground=t['btn_primary'], activeforeground=t['btn_primary_fg'])
        self.btn_suspend.configure(bg=t['btn_suspend'], fg=t['btn_suspend_fg'],
                                   activebackground=t['btn_suspend'], activeforeground=t['btn_suspend_fg'])

    def toggle_theme(self):
        self.current_theme = "dark" if self.current_theme == "light" else "light"
        self.apply_theme()
        self.save_config()

    def load_config(self):
        self.config = {}
        if os.path.exists(CONFIG_FILE):
            try:
                with open(CONFIG_FILE, 'r') as f:
                    self.config = json.load(f)
                    self.ip_var.set(self.config.get('last_ip', ''))
                    self.current_theme = self.config.get('theme', 'light')
                    self.always_on_top = self.config.get('always_on_top', False)
            except: 
                self.current_theme = "light"
                self.always_on_top = False
        else: 
            self.current_theme = "light"
            self.always_on_top = False

    def save_config(self):
        try:
            self.config['last_ip'] = self.ip_var.get().strip()
            self.config['theme'] = self.current_theme
            self.config['always_on_top'] = self.always_on_top
            self.config['window_width'] = self.root.winfo_width()
            self.config['window_height'] = self.root.winfo_height()
            with open(CONFIG_FILE, 'w') as f:
                json.dump(self.config, f)
        except: pass
    
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

    def send_cmd(self, endpoint, log_text):
        if not self.connected: return
        def _req():
            try:
                requests.get(f"{self.base_url}{endpoint}", timeout=2)
                self.root.after(0, lambda: self.show_log(log_text))
            except:
                self.root.after(0, lambda: self.show_log("失败", True))
        threading.Thread(target=_req, daemon=True).start()

    def previous_page(self): self.send_cmd("/koreader/event/GotoViewRel/-1", "上一页")
    def next_page(self): self.send_cmd("/koreader/event/GotoViewRel/1", "下一页")
    def suspend_device(self): self.send_cmd("/koreader/event/RequestSuspend", "已发送休眠")

    def on_mouse_wheel(self, event):
        if not self.connected: return
        if event.delta > 0: self.previous_page()
        else: self.next_page()

    def show_log(self, text, is_error=False):
        t = THEMES[self.current_theme]
        color = t['danger'] if is_error else t['fg_secondary']
        time = datetime.now().strftime("%H:%M:%S")
        self.lbl_status.config(text=f"{time} - {text}", fg=color)

def main():
    root = tk.Tk()
    try: from ctypes import windll; windll.shcore.SetProcessDpiAwareness(1)
    except: pass
    app = KOReaderRemoteApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()