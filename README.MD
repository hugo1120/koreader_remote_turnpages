
<div align="center">

# KOReader Remote Control / KOReader 翻页助手

[![Python](https://img.shields.io/badge/Python-3.x-blue.svg)](https://www.python.org/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

<a href="#english">English</a> | <a href="#chinese">中文</a>

</div>

---

<div id="english"></div>

## 📖 English

### Introduction
**KOReader Remote Control** is a lightweight desktop application designed to control your e-reader running [KOReader](https://github.com/koreader/koreader) over a local network. It allows you to turn pages, refresh the screen, rotate the display, and take screenshots directly from your PC.

### Features
- **Remote Page Turning**: Control page navigation using buttons, arrow keys, mouse wheel, or **Xbox Controller**.
- **Connection Management**: Connect to your device via its IP address.
- **Screen Controls**:
  - ⟳ **Rotate Screen**: Toggle between portrait and landscape modes.
  - ⚡ **Full Refresh**: Trigger a full E-ink screen refresh.
  - 📷 **Screenshot**: Capture the device screen and save it locally.
  - 😴 **Suspend**: Put the device to sleep remotely.
- **Customizable UI**:
  - 🌓 **Light/Dark Mode**: Switch between light and dark themes.
  - 📌 **Always on Top**: Keep the control window above other applications.
  - **Minimalist Design**: Compact interface to save screen space.

### Installation
1. Ensure you have Python 3.x installed.
2. Install the required dependencies:
   ```bash
   pip install requests pygame
   ```
3. (Optional) If you want to build the executable yourself:
   ```bash
   pip install pyinstaller
   pyinstaller --noconsole --onefile --icon=icon.ico KOReader_Pro.py
   ```

### Usage
1. **Enable SSH/HTTP Server on KOReader**:
   - On your e-reader, go to **Network** -> **SSH/HTTP Server** and enable it.
   - Note the IP address displayed.
2. **Run the Application**:
   - Run `KOReader_Pro.py` or the executable file.
3. **Connect**:
   - Enter the IP address of your e-reader and click **Connect**.
4. **Control**:
   - Use the buttons or keyboard shortcuts to control your device.

### Keyboard Shortcuts
| Key | Action |
| :--- | :--- |
| `Left` / `Up` / `PageUp` | Previous Page |
| `Right` / `Down` / `PageDown` | Next Page |
| `Mouse Wheel` | Scroll Pages |
| `Esc` | Suspend Device |

### Gamepad Controls (Xbox)
| Button | Action |
| :--- | :--- |
| `D-Pad Up` / `Right` | Next Page |
| `D-Pad Down` / `Left` | Previous Page |
| `Y` | Rotate Screen |
| `X` | Full Refresh |
| `A` | Screenshot |
| `B` | Suspend Device |
| `Start` | Disconnect |

### Customizing Controls
The key mapping is stored in `koreader_config.json`, which is generated after the first run. You can modify it to customize your controls.

**Example `gamepad_mapping` in `koreader_config.json`:**
```json
"gamepad_mapping": {
    "next_page": ["DPAD_UP", "DPAD_RIGHT"],
    "previous_page": ["DPAD_DOWN", "DPAD_LEFT"],
    "rotate": ["Y", "BTN_3"],
    "refresh": ["X", "BTN_2"],
    "screenshot": ["A", "BTN_0"],
    "suspend": ["B", "BTN_1"],
    "disconnect": ["START", "BTN_7"]
}
```

**Supported Keys:**
You can use either the alias (Recommended) or the raw button ID.

| Button | Alias | ID Code |
| :--- | :--- | :--- |
| **A** | `"A"` | `"BTN_0"` |
| **B** | `"B"` | `"BTN_1"` |
| **X** | `"X"` | `"BTN_2"` |
| **Y** | `"Y"` | `"BTN_3"` |
| **LB** | `"LB"` | `"BTN_4"` |
| **RB** | `"RB"` | `"BTN_5"` |
| **Back/View** | `"BACK"` | `"BTN_6"` |
| **Start/Menu** | `"START"` | `"BTN_7"` |
| **Left Stick Click** | - | `"BTN_8"` |
| **Right Stick Click** | - | `"BTN_9"` |
| **D-Pad** | `"DPAD_UP"`, `"DPAD_DOWN"`, `"DPAD_LEFT"`, `"DPAD_RIGHT"` | |

---

<div id="chinese"></div>

## 🇨🇳 中文

### 简介
**KOReader 翻页助手** 是一款轻量级的桌面应用程序，旨在通过局域网控制运行 [KOReader](https://github.com/koreader/koreader) 的电子书阅读器。它允许您在电脑上进行翻页、刷新屏幕、旋转屏幕以及截图等操作。

### 功能特性
- **远程翻页**: 使用按钮、方向键、鼠标滚轮或 **Xbox 手柄** 控制页面翻页。
- **连接管理**: 通过 IP 地址连接设备。
- **屏幕控制**:
  - ⟳ **屏幕旋转**: 在横屏和竖屏模式之间切换。
  - ⚡ **全刷屏幕**: 触发电子墨水屏的全屏刷新。
  - 📷 **屏幕截图**: 获取设备当前屏幕截图并保存到本地 `screenshots` 文件夹。
  - 😴 **设备休眠**: 远程让设备进入休眠状态。
- **个性化界面**:
  - 🌓 **深色/浅色模式**: 只有两种精心调配的主题。
  - 📌 **窗口置顶**: 保持控制窗口在最顶层，方便阅读时操作。
  - **极简设计**: 紧凑的界面设计，占用极少的屏幕空间。

### 安装说明
1. 确保已安装 Python 3.x。
2. 安装必要的依赖库：
   ```bash
   pip install requests pygame
   ```
3. (可选) 如果您想自己打包成 exe 文件：
   ```bash
   pip install pyinstaller
   pyinstaller --noconsole --onefile --icon=icon.ico KOReader_Pro.py
   ```

### 使用指南
1. **在 KOReader 上开启 SSH/HTTP 服务**:
   - 在您的电子书阅读器上，进入 **网络 (Network)** -> **SSH/HTTP Server** 并开启它。
   - 记下显示的 IP 地址。
2. **运行程序**:
   - 运行 `KOReader_Pro.py` 脚本或编译好的可执行文件。
3. **连接设备**:
   - 输入您电子书阅读器的 IP 地址，点击 **立即连接**。
4. **开始控制**:
   - 使用界面按钮或键盘快捷键控制设备。

### 快捷键列表
| 按键 | 功能 |
| :--- | :--- |
| `←` / `↑` / `PageUp` | 上一页 |
| `→` / `↓` / `PageDown` | 下一页 |
| `鼠标滚轮` | 翻页 |
| `Esc` | 设备休眠 |

### 手柄控制 (Xbox)
| 按键 | 功能 |
| :--- | :--- |
| `十字键 上` / `右` | 下一页 |
| `十字键 下` / `左` | 上一页 |
| `Y` | 旋转屏幕 |
| `X` | 全刷屏幕 |
| `A` | 屏幕截图 |
| `B` | 设备休眠 |
| `Start` | 断开连接 |

### 自定义按键配置
程序首次运行后，会在同目录下生成 `koreader_config.json` 文件。您可以打开该文件修改 `gamepad_mapping` 部分来自定义按键。

**配置示例:**
```json
"gamepad_mapping": {
    "next_page": ["DPAD_UP", "DPAD_RIGHT"],  // 下一页
    "previous_page": ["DPAD_DOWN", "DPAD_LEFT"], // 上一页
    "rotate": ["Y", "BTN_3"],    // 旋转
    "refresh": ["X", "BTN_2"],   // 刷新
    "screenshot": ["A", "BTN_0"], // 截图
    "suspend": ["B", "BTN_1"],   // 休眠
    "disconnect": ["START"]      // 断开
}
```

**支持的按键代码:**
推荐直接使用双引号包裹的别名（如 `"A"`, `"START"`），也可以使用原始 ID。

| Xbox 按键 | 别名 (推荐) | 原始 ID 代码 |
| :--- | :--- | :--- |
| **A** | `"A"` | `"BTN_0"` |
| **B** | `"B"` | `"BTN_1"` |
| **X** | `"X"` | `"BTN_2"` |
| **Y** | `"Y"` | `"BTN_3"` |
| **LB (左肩键)** | `"LB"` | `"BTN_4"` |
| **RB (右肩键)** | `"RB"` | `"BTN_5"` |
| **Back / View** | `"BACK"` | `"BTN_6"` |
| **Start / Menu** | `"START"` | `"BTN_7"` |
| **左摇杆按下** | - | `"BTN_8"` |
| **右摇杆按下** | - | `"BTN_9"` |
| **十字键** | `"DPAD_UP"`, `"DPAD_DOWN"`, `"DPAD_LEFT"`, `"DPAD_RIGHT"` | |

---
<div align="center">
    <a href="#english">Go to Top (English)</a> | <a href="#chinese">回到顶部 (中文)</a>
</div>
