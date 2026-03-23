# KOReader Page Turner

Desktop remote control for KOReader on the local network.

## English

### Overview
`KOReader Page Turner` lets you control a KOReader device from Windows. It supports page turning, full refresh, rotation, suspend, screenshots, keyboard shortcuts, mouse wheel, and Xbox-compatible gamepads.

### Requirements
- Python 3.x
- KOReader HTTP Server enabled on the device

### Install
```bash
python -m pip install requests pygame
```

### Run
```bash
python "koreader_page_turner.py"
```

### Build Single EXE
```bash
build_exe.bat
```

Output:
```text
dist/KOReader Page Turner.exe
```

### Features
- Page turning with buttons, arrow keys, `PageUp`, `PageDown`, and mouse wheel
- Quick actions: rotate, refresh, screenshot, suspend
- Light / dark theme
- Always-on-top window mode
- Optional gamepad support through `pygame`
- Runtime config saved to `koreader_config.json`

### Keyboard Shortcuts
- `Left` / `Up` / `PageUp`: Previous page
- `Right` / `Down` / `PageDown`: Next page
- `Mouse Wheel`: Turn pages
- `Esc`: Suspend device

## 中文

### 简介
`KOReader Page Turner` 是一个用于局域网控制 KOReader 的 Windows 桌面工具，支持翻页、旋转、全刷、截图、休眠，以及键盘、鼠标滚轮、Xbox 手柄控制。

### 环境要求
- Python 3.x
- 阅读器端已开启 KOReader 的 HTTP Server

### 安装依赖
```bash
python -m pip install requests pygame
```

### 运行
```bash
python "koreader_page_turner.py"
```

### 打包单文件 EXE
```bash
build_exe.bat
```

生成结果：
```text
dist/KOReader Page Turner.exe
```

### 使用说明
1. 在 KOReader 中开启 `Network -> SSH/HTTP Server`
2. 启动 `koreader_page_turner.py` 或打包后的 exe
3. 输入设备 IP 地址并连接
4. 使用界面按钮、键盘、滚轮或手柄进行控制

### 主要功能
- 上一页 / 下一页翻页
- 屏幕旋转与全刷
- 保存设备截图到 `screenshots/`
- 设备休眠
- 浅色 / 深色主题切换
- 窗口置顶
- 自定义手柄映射

### 快捷键
- `↑` / `←` / `PageUp`：上一页
- `↓` / `→` / `PageDown`：下一页
- `鼠标滚轮`：翻页
- `Esc`：设备休眠

### 配置文件
首次运行后会在程序目录生成 `koreader_config.json`，用于保存：
- 最近连接的 IP
- 当前主题
- 窗口大小
- 手柄按键映射
