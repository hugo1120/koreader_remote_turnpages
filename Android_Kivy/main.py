from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.anchorlayout import AnchorLayout
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.button import Button
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.core.window import Window
from kivy.storage.jsonstore import JsonStore
from kivy.clock import Clock
from kivy.utils import platform # 用于判断是否在Android
import requests
import threading
from datetime import datetime
import os

# --- 配色方案 (极简风) ---
THEMES = {
    "light": {
        "window_bg": [0.96, 0.96, 0.96, 1],    # #F5F5F5
        "fg_primary": [0, 0, 0, 1],            # #000000
        "fg_secondary": [0.4, 0.4, 0.4, 1],    # #666666
        "input_bg": [1, 1, 1, 1],              # #FFFFFF
        "input_fg": [0, 0, 0, 1],              # #000000
        "btn_primary": [0, 0.4, 0.75, 1],      # #0067C0
        "btn_primary_fg": [1, 1, 1, 1],
        "btn_secondary": [0.88, 0.88, 0.88, 1],# #E0E0E0
        "btn_secondary_fg": [0, 0, 0, 1],
        "btn_suspend": [0.38, 0.49, 0.55, 1],  # #607D8B
        "danger": [0.83, 0.18, 0.18, 1]        # #D32F2F
    },
    "dark": {
        "window_bg": [0.12, 0.12, 0.12, 1],    # #1E1E1E
        "fg_primary": [1, 1, 1, 1],            # #FFFFFF
        "fg_secondary": [0.67, 0.67, 0.67, 1], # #AAAAAA
        "input_bg": [0.2, 0.2, 0.2, 1],        # #333333
        "input_fg": [1, 1, 1, 1],              # #FFFFFF
        "btn_primary": [0.3, 0.76, 1, 1],      # #4CC2FF
        "btn_primary_fg": [0, 0, 0, 1],
        "btn_secondary": [0.24, 0.24, 0.26, 1],# #3E3E42
        "btn_secondary_fg": [1, 1, 1, 1],
        "btn_suspend": [0.27, 0.35, 0.39, 1],  # #455A64
        "danger": [1, 0.32, 0.32, 1]           # #FF5252
    }
}

class KOReaderApp(App):
    def build(self):
        self.title = "Hugoの翻页助手"
        self.store = JsonStore('koreader_config.json')
        self.base_url = ""
        self.connected = False
        self.current_theme_name = self.store.get('theme')['name'] if self.store.exists('theme') else 'light'
        self.rotation_state = 0
        
        # 屏幕管理器
        self.sm = ScreenManager()
        
        # 登录页 screen_login
        self.screen_login = Screen(name='login')
        self.build_login_screen()
        self.sm.add_widget(self.screen_login)
        
        # 控制页 screen_control
        self.screen_control = Screen(name='control')
        self.build_control_screen()
        self.sm.add_widget(self.screen_control)
        
        # 绑定按键 (音量键)
        Window.bind(on_key_down=self._on_keyboard_down)
        
        # 初始化主题
        self.apply_theme(self.current_theme_name)
        
        # 自动填充IP
        if self.store.exists('connection'):
             last_ip = self.store.get('connection')['ip']
             self.input_ip.text = last_ip

        return self.sm

    def build_login_screen(self):
        layout = BoxLayout(orientation='vertical', padding=[20, 50, 20, 20], spacing=20)
        
        # 标题
        self.lbl_title = Label(text="连接设备", font_size='24sp', bold=True, size_hint_y=None, height=60)
        layout.add_widget(self.lbl_title)
        
        # IP 输入
        self.input_ip = TextInput(hint_text="输入 IP (例如 192.168.1.5)", multiline=False, size_hint_y=None, height=50, font_size='18sp')
        layout.add_widget(self.input_ip)
        
        # 占位
        layout.add_widget(Label(size_hint_y=1))
        
        # 切换主题按钮
        self.btn_theme_login = Button(text="切换主题 ☀/☾",size_hint_y=None, height=50, on_release=self.toggle_theme)
        layout.add_widget(self.btn_theme_login)

        # 连接按钮
        self.btn_connect = Button(text="立即连接", size_hint_y=None, height=60, font_size='18sp', on_release=self.on_connect)
        layout.add_widget(self.btn_connect)
        
        self.screen_login.add_widget(layout)

    def build_control_screen(self):
        root_layout = BoxLayout(orientation='vertical', spacing=0)
        
        # 顶部栏 (Info + Actions)
        top_bar = BoxLayout(orientation='horizontal', size_hint_y=None, height=50, padding=5, spacing=5)
        
        self.lbl_status = Label(text="已连接", font_size='14sp', halign='left', valign='middle')
        self.lbl_status.bind(size=self.lbl_status.setter('text_size')) # 用于对齐
        top_bar.add_widget(self.lbl_status)
        
        # 顶部右侧按钮群
        self.btn_theme_ctrl = Button(text="☾", size_hint_x=None, width=50, on_release=self.toggle_theme)
        top_bar.add_widget(self.btn_theme_ctrl)

        self.btn_disconnect = Button(text="断开", size_hint_x=None, width=60, on_release=self.disconnect)
        top_bar.add_widget(self.btn_disconnect)
        
        root_layout.add_widget(top_bar)
        
        # 中间：翻页按钮区
        grid = GridLayout(cols=2, spacing=10, padding=10)
        
        # 上一页
        self.btn_prev = Button(text="< 上一页\n(音量+)", font_size='20sp', halign='center', on_release=lambda x: self.previous_page())
        grid.add_widget(self.btn_prev)
        
        # 下一页
        self.btn_next = Button(text="下一页 >\n(音量-)", font_size='24sp', bold=True, halign='center', on_release=lambda x: self.next_page())
        grid.add_widget(self.btn_next)
        
        root_layout.add_widget(grid)
        
        # 底部：功能按钮区
        bottom_box = BoxLayout(orientation='vertical', size_hint_y=None, height=120, padding=10, spacing=10)
        
        # 第一排功能
        row1 = BoxLayout(spacing=10)
        self.btn_rotate = Button(text="⟳ 旋转", on_release=lambda x: self.rotate_device())
        self.btn_refresh = Button(text="⚡ 全刷", on_release=lambda x: self.full_refresh())
        row1.add_widget(self.btn_rotate)
        row1.add_widget(self.btn_refresh)
        bottom_box.add_widget(row1)
        
        # 休眠按钮
        self.btn_suspend = Button(text="😴 设备休眠", on_release=lambda x: self.suspend_device())
        bottom_box.add_widget(self.btn_suspend)
        
        root_layout.add_widget(bottom_box)
        
        self.screen_control.add_widget(root_layout)
        
        # 引用所有需要这样色的组件，方便 theme 切换
        self.ui_elements = {
            'window': [self.screen_login, self.screen_control], # Kivy 设置背景色稍微麻烦点，通常用 Canvas，这里简化处理，只设clearcolor
            'lbl_title': self.lbl_title,
            'input_ip': self.input_ip,
            'log_text': self.lbl_status,
            'btn_connect': self.btn_connect,
            'btn_prev': self.btn_prev,
            'btn_next': self.btn_next,
            'btn_suspend': self.btn_suspend,
            'btn_secondary': [self.btn_rotate, self.btn_refresh, self.btn_theme_login, self.btn_theme_ctrl]
        }

    def apply_theme(self, theme_name):
        self.current_theme_name = theme_name
        t = THEMES[theme_name]
        
        # Save config
        self.store.put('theme', name=theme_name)
        
        # Apply Logic
        Window.clearcolor = t['window_bg']
        
        # Labels
        self.lbl_title.color = t['fg_primary']
        self.lbl_status.color = t['fg_primary']
        
        # Inputs
        self.input_ip.background_color = t['input_bg']
        self.input_ip.foreground_color = t['input_fg']
        
        # Buttons logic
        def set_btn(btn, bg, fg):
            btn.background_normal = '' # Remove default Image
            btn.background_color = bg
            btn.color = fg

        set_btn(self.btn_connect, t['btn_primary'], t['btn_primary_fg'])
        set_btn(self.btn_next, t['btn_primary'], t['btn_primary_fg'])
        
        set_btn(self.btn_prev, t['btn_secondary'], t['btn_secondary_fg'])
        for btn in self.ui_elements['btn_secondary']:
            set_btn(btn, t['btn_secondary'], t['btn_secondary_fg'])
            
        set_btn(self.btn_suspend, t['btn_suspend'], [1,1,1,1])
        set_btn(self.btn_disconnect, t['window_bg'], t['danger']) # Text style button

    def toggle_theme(self, instance):
        new_theme = "dark" if self.current_theme_name == "light" else "light"
        self.apply_theme(new_theme)

    def on_connect(self, instance):
        ip = self.input_ip.text.strip()
        if not ip: return
        
        self.base_url = f"http://{ip}:8080"
        self.btn_connect.text = "连接中..."
        self.btn_connect.disabled = True
        
        threading.Thread(target=self.thread_check_connection, args=(ip,), daemon=True).start()

    def thread_check_connection(self, ip):
        try:
            resp = requests.get(f"{self.base_url}/koreader/event/GotoViewRel/0", timeout=3)
            if resp.status_code == 200:
                Clock.schedule_once(lambda dt: self.handle_connect_success(ip))
            else:
                raise Exception("Error")
        except:
            Clock.schedule_once(lambda dt: self.handle_connect_fail())

    def handle_connect_success(self, ip):
        self.connected = True
        self.store.put('connection', ip=ip)
        self.lbl_status.text = f"已连接: {ip}"
        self.sm.current = 'control'
        self.btn_connect.text = "立即连接"
        self.btn_connect.disabled = False

    def handle_connect_fail(self):
        self.btn_connect.text = "连接失败，请重试"
        self.btn_connect.disabled = False

    def disconnect(self, instance):
        self.connected = False
        self.sm.current = 'login'

    def send_cmd(self, endpoint, log_msg=""):
        if not self.connected: return
        def _req():
            try:
                requests.get(f"{self.base_url}{endpoint}", timeout=2)
                if log_msg:
                    Clock.schedule_once(lambda dt: self.update_status(log_msg))
            except:
                Clock.schedule_once(lambda dt: self.update_status("请求失败"))
        threading.Thread(target=_req, daemon=True).start()

    def update_status(self, text):
        time = datetime.now().strftime("%H:%M:%S")
        self.lbl_status.text = f"{time} - {text}"

    # === Actions ===
    def previous_page(self): self.send_cmd("/koreader/event/GotoViewRel/-1", "上一页")
    def next_page(self): self.send_cmd("/koreader/event/GotoViewRel/1", "下一页")
    def suspend_device(self): self.send_cmd("/koreader/event/RequestSuspend", "发送休眠")
    def full_refresh(self): self.send_cmd("/koreader/event/FullRefresh", "请求刷新")
    
    def rotate_device(self):
        self.rotation_state = 1 if self.rotation_state == 0 else 0
        self.send_cmd(f"/koreader/event/SetRotationMode/{self.rotation_state}", "旋转屏幕")

    # === Key Binding ===
    def _on_keyboard_down(self, window, key, scancode, codepoint, modifiers):
        # key 24: Volume Up -> Previous
        # key 25: Volume Down -> Next
        # key 273: Up Arrow (PC Test)
        # key 274: Down Arrow (PC Test)
        
        if not self.connected: return False # Don't consume if not connected
        
        if key == 24 or key == 273: # VolUp or Up
            self.previous_page()
            return True # Consume event
        elif key == 25 or key == 274: # VolDown or Down
            self.next_page()
            return True # Consume event
        
        return False

if __name__ == '__main__':
    KOReaderApp().run()
