import tkinter as tk
from tkinter import messagebox, ttk
import requests
import threading
import json
import os
from datetime import datetime

# --- 配置常量 (Win11 风格配色) ---
COLOR_BG = "#F3F3F3"          # 窗口背景色 (Win11 浅灰)
COLOR_WHITE = "#FFFFFF"       # 内容区背景
COLOR_PRIMARY = "#0067C0"     # Win11 主题蓝
COLOR_PRIMARY_HOVER = "#1975CA"
COLOR_SECONDARY = "#E1E1E1"   # 次要按钮背景
COLOR_TEXT = "#202020"        # 主要文字颜色
COLOR_TEXT_LIGHT = "#666666"  # 次要文字颜色
FONT_MAIN = ("Segoe UI", 10)
FONT_BOLD = ("Segoe UI", 10, "bold")
FONT_LARGE = ("Segoe UI", 12, "bold")

CONFIG_FILE = 'koreader_config.json'

class KOReaderRemoteApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Kindle 翻页助手")
        # 增加窗口大小以防止文字截断
        self.root.geometry("360x260")
        
        # 居中显示窗口
        self.center_window(360, 260)
        
        # 基础配置
        self.root.configure(bg=COLOR_BG)
        self.root.resizable(False, False)
        
        # 状态变量
        self.connected = False
        self.base_url = ""
        self.ip_var = tk.StringVar()
        
        # 设置样式
        self.setup_styles()
        
        # 加载历史IP
        self.load_last_ip()
        
        # 初始化界面
        self.create_widgets()
        
        # 键盘绑定 (全局)
        self.root.bind('<Prior>', lambda e: self.previous_page())  # Page Up
        self.root.bind('<Next>', lambda e: self.next_page())       # Page Down
        self.root.bind('<Left>', lambda e: self.previous_page())   # 左箭头
        self.root.bind('<Right>', lambda e: self.next_page())      # 右箭头
        self.root.bind('<MouseWheel>', self.on_mouse_wheel)        # 滚轮

    def center_window(self, width, height):
        """窗口居中"""
        screen_width = self.root.winfo_screenwidth()
        screen_height = self.root.winfo_screenheight()
        x = (screen_width - width) // 2
        y = (screen_height - height) // 2
        self.root.geometry(f'{width}x{height}+{x}+{y}')

    def setup_styles(self):
        """配置 TTK 样式以接近 Win11 风格"""
        style = ttk.Style()
        style.theme_use('clam') # 使用 clam 作为基础引擎更容易自定义
        
        # 框架背景
        style.configure("TFrame", background=COLOR_BG)
        style.configure("Card.TFrame", background=COLOR_WHITE, relief="flat")
        
        # 输入框
        style.configure("TEntry", 
                        fieldbackground=COLOR_WHITE,
                        borderwidth=1,
                        relief="solid",
                        padding=5)
        
        # 主要按钮 (蓝色)
        style.configure("Primary.TButton",
                        font=FONT_BOLD,
                        background=COLOR_PRIMARY,
                        foreground="white",
                        borderwidth=0,
                        focuscolor=COLOR_PRIMARY,
                        padding=8)
        style.map("Primary.TButton",
                  background=[('active', COLOR_PRIMARY_HOVER), ('disabled', '#CCCCCC')])
        
        # 次要按钮 (灰色/红色)
        style.configure("Secondary.TButton",
                        font=FONT_MAIN,
                        background=COLOR_SECONDARY,
                        foreground=COLOR_TEXT,
                        borderwidth=0,
                        focuscolor=COLOR_SECONDARY,
                        padding=6)
        style.map("Secondary.TButton",
                  background=[('active', '#D1D1D1')])
                  
        # 红色断开按钮
        style.configure("Danger.TButton",
                        font=("Segoe UI", 9),
                        background="#FFFFFF",
                        foreground="#D13438", # Win11 Red
                        borderwidth=0,
                        padding=4)
        style.map("Danger.TButton",
                  background=[('active', '#FEF2F2')])

    def create_widgets(self):
        # === 页面容器 ===
        self.container = tk.Frame(self.root, bg=COLOR_BG)
        self.container.pack(fill=tk.BOTH, expand=True, padx=25, pady=25)
        
        # === 连接页面 (Login View) ===
        self.login_frame = ttk.Frame(self.container)
        
        # 标题
        lbl_title = tk.Label(self.login_frame, text="连接设备", font=("Segoe UI", 16, "bold"), bg=COLOR_BG, fg=COLOR_TEXT)
        lbl_title.pack(pady=(5, 20), anchor="w")
        
        # IP 输入区域 (白色卡片风格)
        input_card = ttk.Frame(self.login_frame, style="Card.TFrame", padding=15)
        input_card.pack(fill=tk.X, pady=(0, 20))
        
        # 确保使用 pady 而不是 mb
        tk.Label(input_card, text="Kindle IP 地址", font=("Segoe UI", 9), bg=COLOR_WHITE, fg=COLOR_TEXT_LIGHT).pack(anchor="w", pady=(0, 8))
        
        self.entry_ip = ttk.Entry(input_card, textvariable=self.ip_var, font=("Segoe UI", 11))
        self.entry_ip.pack(fill=tk.X)
        self.entry_ip.bind('<Return>', lambda e: self.on_connect_click())
        
        # 连接按钮
        self.btn_connect = ttk.Button(self.login_frame, text="连接", style="Primary.TButton", command=self.on_connect_click)
        self.btn_connect.pack(fill=tk.X, ipady=6)

        # === 翻页控制页面 (Control View) ===
        self.control_frame = ttk.Frame(self.container)
        
        # 顶部栏 (设备信息 + 断开)
        header_frame = tk.Frame(self.control_frame, bg=COLOR_BG)
        header_frame.pack(fill=tk.X, pady=(0, 15))
        
        self.lbl_device = tk.Label(header_frame, text="已连接", font=FONT_BOLD, bg=COLOR_BG, fg=COLOR_PRIMARY)
        self.lbl_device.pack(side=tk.LEFT)
        
        btn_disconnect = ttk.Button(header_frame, text="断开", style="Danger.TButton", command=self.disconnect)
        btn_disconnect.pack(side=tk.RIGHT)
        
        # 翻页按钮区域 (两列布局)
        action_frame = tk.Frame(self.control_frame, bg=COLOR_BG)
        action_frame.pack(fill=tk.BOTH, expand=True)
        
        # 上一页 (左侧，较小或灰色)
        self.btn_prev = tk.Button(action_frame, text="< 上一页", 
                                  font=FONT_MAIN,
                                  bg="white", fg=COLOR_TEXT,
                                  relief=tk.FLAT, bd=0,
                                  activebackground="#E5E5E5",
                                  cursor="hand2",
                                  command=self.previous_page)
        self.btn_prev.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=(0, 8))
        
        # 下一页 (右侧，较大或蓝色)
        self.btn_next = tk.Button(action_frame, text="下一页 >", 
                                  font=FONT_LARGE,
                                  bg=COLOR_PRIMARY, fg="white",
                                  relief=tk.FLAT, bd=0,
                                  activebackground=COLOR_PRIMARY_HOVER,
                                  activeforeground="white",
                                  cursor="hand2",
                                  command=self.next_page)
        self.btn_next.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=(8, 0))
        
        # 底部微型日志
        self.lbl_status = tk.Label(self.control_frame, text="就绪", font=("Segoe UI", 9), bg=COLOR_BG, fg=COLOR_TEXT_LIGHT)
        self.lbl_status.pack(fill=tk.X, pady=(15, 0), anchor="w")

        # 初始显示
        self.show_login()

    # --- 界面切换逻辑 ---
    
    def show_login(self):
        self.control_frame.pack_forget()
        self.login_frame.pack(fill=tk.BOTH, expand=True)
        self.entry_ip.focus()

    def show_control(self):
        self.login_frame.pack_forget()
        self.control_frame.pack(fill=tk.BOTH, expand=True)
        self.lbl_device.config(text=f"Kindle ({self.ip_var.get()})")

    # --- 核心逻辑 ---

    def load_last_ip(self):
        """加载配置"""
        if os.path.exists(CONFIG_FILE):
            try:
                with open(CONFIG_FILE, 'r') as f:
                    config = json.load(f)
                    self.ip_var.set(config.get('last_ip', ''))
            except:
                pass

    def save_current_ip(self):
        """仅在连接成功后保存IP"""
        try:
            config = {'last_ip': self.ip_var.get().strip()}
            with open(CONFIG_FILE, 'w') as f:
                json.dump(config, f)
        except:
            pass

    def on_connect_click(self):
        ip = self.ip_var.get().strip()
        if not ip:
            messagebox.showwarning("提示", "请输入 IP 地址")
            return
            
        self.btn_connect.config(state=tk.DISABLED, text="连接中...")
        self.base_url = f"http://{ip}:8080"
        
        # 开启线程测试连接
        threading.Thread(target=self.thread_check_connection, daemon=True).start()

    def thread_check_connection(self):
        try:
            # 尝试访问 Kindle
            resp = requests.get(f"{self.base_url}/koreader/event/GotoViewRel/0", timeout=3)
            if resp.status_code == 200:
                self.root.after(0, self.handle_connect_success)
            else:
                raise Exception("Status Code Error")
        except Exception as e:
            # 【重要修复】先捕获错误信息，防止 lambda 执行时 e 变量已销毁
            err_msg = str(e)
            self.root.after(0, lambda: self.handle_connect_fail(err_msg))

    def handle_connect_success(self):
        self.connected = True
        self.save_current_ip() # 成功才保存
        self.btn_connect.config(state=tk.NORMAL, text="连接")
        self.show_control()
        self.show_log("连接成功")

    def handle_connect_fail(self, error_msg):
        self.btn_connect.config(state=tk.NORMAL, text="连接")
        messagebox.showerror("连接失败", "无法连接到 Kindle。\n请检查：\n1. IP地址是否正确\n2. Kindle 是否开启了 HTTP Server")

    def disconnect(self):
        self.connected = False
        self.show_login()

    def previous_page(self):
        self.send_command(-1)

    def next_page(self):
        self.send_command(1)
    
    def on_mouse_wheel(self, event):
        if not self.connected: return
        # Windows: event.delta 120 (up) / -120 (down)
        if event.delta > 0:
            self.previous_page()
        else:
            self.next_page()

    def send_command(self, direction):
        if not self.connected: return
        
        def _req():
            try:
                url = f"{self.base_url}/koreader/event/GotoViewRel/{direction}"
                requests.get(url, timeout=2)
                # 成功不弹窗，只更新底部微型日志
                msg = "翻至上一页" if direction == -1 else "翻至下一页"
                self.root.after(0, lambda: self.show_log(msg))
            except:
                self.root.after(0, lambda: self.show_log("命令发送失败", True))

        threading.Thread(target=_req, daemon=True).start()

    def show_log(self, text, is_error=False):
        """底部微型日志更新"""
        color = "#D13438" if is_error else COLOR_TEXT_LIGHT
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.lbl_status.config(text=f"{timestamp} - {text}", fg=color)

def main():
    root = tk.Tk()
    # 尝试设置高DPI支持 (解决Win10/11下模糊问题)
    try:
        from ctypes import windll
        windll.shcore.SetProcessDpiAwareness(1)
    except:
        pass
        
    app = KOReaderRemoteApp(root)
    root.mainloop()

if __name__ == "__main__":
    main()