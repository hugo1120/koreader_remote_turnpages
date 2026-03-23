# KOReader Page Turner

Windows desktop remote for KOReader over the local network.

## Overview

`KOReader Page Turner` is a small Tkinter desktop tool for controlling KOReader from a Windows PC. It supports page turning, full refresh, rotation, suspend, screenshots, keyboard shortcuts, mouse wheel control, and optional gamepad input.

## Features

- Previous page / next page buttons
- Keyboard shortcuts: `Left`, `Right`, `Up`, `Down`, `PageUp`, `PageDown`
- Mouse wheel page turning
- Quick actions: rotate, refresh, screenshot, suspend
- Light / dark theme
- Always-on-top mode
- Optional `pygame` gamepad support
- Local config persistence in `koreader_config.json`
- Single-file Windows EXE packaging

## Requirements

- Windows
- Python 3.x
- KOReader with HTTP Server enabled

Enable it on the device:

```text
Network -> SSH/HTTP Server
```

## Install

Minimum runtime dependencies:

```bash
python -m pip install requests
```

Optional gamepad support:

```bash
python -m pip install pygame
```

If `pygame` fails to import, the app still starts normally. Only gamepad control is disabled.

## Run

```bash
python "koreader_page_turner.py"
```

## Build Single EXE

Use the included script:

```bash
build_exe.bat
```

Or run PyInstaller manually:

```bash
python -m PyInstaller --noconsole --onefile --clean --name "KOReader Page Turner" --icon "logo.ico" --add-data "logo.ico;." --add-data "logo.png;." "koreader_page_turner.py"
```

Build output:

```text
dist/KOReader Page Turner.exe
```

## Usage

1. Enable KOReader HTTP Server on the device.
2. Start `koreader_page_turner.py` or the packaged EXE.
3. Enter the device IP address.
4. Connect and control page turns from the desktop app.

## Runtime Files

Generated locally in the application directory:

- `koreader_config.json`: saved IP, theme, window size, and gamepad mapping
- `screenshots/`: captured device screenshots

These files should stay local and should not be committed.

## Notes

- The app icon is loaded from `logo.ico`, and `logo.png` is bundled for Tk window icon compatibility.
- The packaged EXE is intended to show the icon both in the taskbar and in the top-left window corner.
- If you use a Python environment with incompatible `pygame` binaries, only the gamepad feature is affected.

## 中文说明

`KOReader Page Turner` 是一个用于局域网控制 KOReader 的 Windows 桌面工具，基于 Tkinter 开发。

主要功能：

- 上一页 / 下一页
- 键盘、滚轮、可选手柄控制
- 旋转、全刷、截图、休眠
- 浅色 / 深色主题
- 窗口置顶
- 打包为单文件 EXE

运行步骤：

1. 在 KOReader 中开启 `Network -> SSH/HTTP Server`
2. 运行 `python "koreader_page_turner.py"` 或打包后的 EXE
3. 输入设备 IP 并连接
4. 使用界面按钮、键盘、滚轮或手柄控制翻页

依赖安装：

```bash
python -m pip install requests
python -m pip install pygame
```

其中 `pygame` 是可选的。若导入失败，程序仍可启动，只是手柄功能不可用。
