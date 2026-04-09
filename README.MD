# KOReader Remote

Local network remote tools for KOReader, including a Windows desktop app and a native Android app.

[English](#english) | [中文](#中文) | [Windows Desktop App](#windows-desktop-app) | [Android APK App](#android-apk-app) | [Android README](android-app/README.md)

---

## English

### Overview

This repository currently contains two clients for controlling KOReader over the local network:

- A Windows desktop app built with Tkinter
- A native Android app built with Kotlin + Jetpack Compose

Both clients are designed for the same use case: connect to a KOReader device on the same LAN and send page-turn or device control commands through KOReader's HTTP server.

### Editions

| Client | Platform | Main Entry | Notes |
| --- | --- | --- | --- |
| Windows Desktop App | Windows | `koreader_page_turner.py` | Traditional desktop controller with keyboard, mouse wheel, and optional gamepad support |
| Android APK App | Android 8.0+ | `android-app/` | Native mobile remote with touch UI, volume-key page turning, and APK builds via GitHub Actions |

### Windows Desktop App

Windows desktop remote for KOReader over LAN.

Core features:

- Previous / next page controls
- Keyboard shortcuts: `Left`, `Right`, `Up`, `Down`, `PageUp`, `PageDown`
- Mouse wheel page turning
- Quick actions: rotate, full refresh, screenshot, suspend
- Light / dark theme
- Always-on-top mode
- Optional `pygame` gamepad support
- Local config persistence in `koreader_config.json`
- Single-file Windows EXE packaging

Quick start:

1. Enable KOReader HTTP Server on your device: `Network -> SSH/HTTP Server`
2. Install dependencies:

```bash
python -m pip install requests
python -m pip install pygame
```

`pygame` is optional. If it cannot be imported, the app still runs without gamepad support.

3. Run the desktop app:

```bash
python "koreader_page_turner.py"
```

4. Build a single EXE if needed:

```bash
build_exe.bat
```

Output:

```text
dist/KOReader Page Turner.exe
```

Relevant files:

- `koreader_page_turner.py`
- `build_exe.bat`
- `logo.ico`
- `logo.png`

### Android APK App

Native Android remote for KOReader.

Core features:

- Button mode and blind gesture mode
- Previous / next page actions
- Volume-key page turning
- Rotate, full refresh, screenshot, suspend
- Light / dark theme toggle
- Remembered host, port, recent hosts, and preferred subnet prefix
- Dedicated mobile UI for phone screens
- Debug / release APK output through GitHub Actions

Android quick links:

- Detailed Android guide: [android-app/README.md](android-app/README.md)
- Android CI workflow: [.github/workflows/android-build.yml](.github/workflows/android-build.yml)

### Repository Layout

- Root directory: Windows desktop app
- `android-app/`: native Android app
- `.github/workflows/android-build.yml`: Android APK CI build
- `agent-docs/`: migration notes and implementation documents

---

## 中文

### 项目概览

这个仓库目前包含两个用于局域网控制 KOReader 的客户端：

- Windows 桌面应用
- 原生 Android 应用

两者都基于 KOReader 的 HTTP Server 工作，适合在同一局域网内远程翻页和执行常用设备控制操作。

### 版本说明

| 客户端 | 平台 | 主要入口 | 说明 |
| --- | --- | --- | --- |
| Windows 桌面应用 | Windows | `koreader_page_turner.py` | 适合桌面环境，支持键盘、滚轮和可选手柄控制 |
| Android APK 应用 | Android 8.0+ | `android-app/` | 原生手机遥控器，支持触屏、音量键翻页和 GitHub Actions 构建 APK |

### Windows 桌面应用

用于局域网控制 KOReader 的 Windows 桌面工具。

主要功能：

- 上一页 / 下一页
- 键盘快捷键：`Left`、`Right`、`Up`、`Down`、`PageUp`、`PageDown`
- 鼠标滚轮翻页
- 旋转、全刷、截图、休眠
- 浅色 / 深色主题
- 窗口置顶
- 可选 `pygame` 手柄支持
- 本地保存配置到 `koreader_config.json`
- 支持打包为单文件 Windows EXE

快速开始：

1. 在 KOReader 中开启 HTTP 服务：`Network -> SSH/HTTP Server`
2. 安装依赖：

```bash
python -m pip install requests
python -m pip install pygame
```

其中 `pygame` 是可选的。若导入失败，程序仍可运行，只是手柄功能不可用。

3. 运行桌面版：

```bash
python "koreader_page_turner.py"
```

4. 如需打包 EXE：

```bash
build_exe.bat
```

输出目录：

```text
dist/KOReader Page Turner.exe
```

相关文件：

- `koreader_page_turner.py`
- `build_exe.bat`
- `logo.ico`
- `logo.png`

### Android APK 应用

用于手机上的原生 KOReader 遥控器。

主要功能：

- 按钮模式与盲操滑动模式
- 上一页 / 下一页
- 音量键翻页
- 旋转、全刷、截图、休眠
- 深浅主题切换
- 记忆 IP、端口、最近连接记录和常用网段
- 面向手机比例优化的触控界面
- 通过 GitHub Actions 产出 Debug / Release APK

Android 相关跳转：

- Android 详细说明：[android-app/README.md](android-app/README.md)
- Android CI 工作流：[.github/workflows/android-build.yml](.github/workflows/android-build.yml)

### 仓库结构

- 仓库根目录：Windows 桌面应用
- `android-app/`：原生 Android 应用
- `.github/workflows/android-build.yml`：Android APK 自动构建
- `agent-docs/`：迁移和实现文档
