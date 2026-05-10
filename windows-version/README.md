# KOReader Remote Windows

Windows 桌面版 KOReader 局域网遥控器，基于 Tkinter + requests 开发，可选 pygame 支持手柄控制。

## 功能

- 上一页 / 下一页
- 键盘快捷键：翻页、旋转、全刷、截图、休眠
- 鼠标滚轮翻页
- 旋转、全刷、截图、休眠
- 顶部设备动作按钮悬停提示
- 浅色 / 深色主题
- 窗口置顶
- 可选 pygame 手柄支持
- 本地配置保存到 `koreader_config.json`
- 支持 PyInstaller 打包单文件 EXE

## 快捷键

键盘快捷键只在本程序窗口获得焦点时触发。鼠标悬停不会触发；窗口置顶也不等于获得键盘焦点。

| 功能 | 默认按键 | KOReader 动作 |
| --- | --- | --- |
| 上一页 | `PageUp` / `Left` / `Up` | `GotoViewRel/-1` |
| 下一页 | `PageDown` / `Right` / `Down` | `GotoViewRel/1` |
| 全刷 | `F5` | `FullRefresh` |
| 旋转 | `F6` | `SetRotationMode` |
| 截图 | `F7` | 保存到 `screenshots/` |
| 休眠 | `Esc` | `RequestSuspend` |

### JSON 自定义

首次运行后会在程序目录生成 `koreader_config.json`。源码运行时该文件在 `windows-version/`；运行打包 EXE 时该文件在 EXE 同目录。可以修改其中的 `keyboard_mapping`：

```json
{
    "keyboard_mapping": {
        "next_page": ["PageDown", "Right", "Down"],
        "previous_page": ["PageUp", "Left", "Up"],
        "refresh": ["F5"],
        "rotate": ["F6"],
        "screenshot": ["F7"],
        "suspend": ["Esc"]
    }
}
```

可用动作名：

- `next_page`
- `previous_page`
- `refresh`
- `rotate`
- `screenshot`
- `suspend`

支持常见别名：`PageUp` / `PgUp`、`PageDown` / `PgDn`、`Esc`、`PrintScreen`、`ArrowLeft` / `ArrowRight` / `ArrowUp` / `ArrowDown`。

## 运行

1. 在 KOReader 中开启 HTTP 服务：`Network -> SSH/HTTP Server`
2. 安装依赖：

```powershell
python -m pip install requests
python -m pip install pygame
```

`pygame` 是可选依赖，缺失时只会禁用手柄功能。

3. 启动桌面版：

```powershell
python "koreader_page_turner.py"
```

## 打包

```powershell
./build_exe.bat
```

输出文件位于：

```text
dist/KOReader Page Turner.exe
```

## 验证

```powershell
python -m unittest "tests/test_http_helpers.py"
python -m unittest "tests/test_keyboard_mapping.py"
python -m py_compile "koreader_page_turner.py"
```

普通控制命令会检查 KOReader HTTP 响应状态；未连接时设备动作会在状态栏提示“请先连接设备”。截图下载复用当前连接的 `base_url`，不再单独拼接固定地址。

## 文件说明

- `koreader_page_turner.py`：桌面版应用入口
- `build_exe.bat`：Windows EXE 打包脚本
- `logo.ico` / `logo.png`：窗口和打包图标
- `koreader_config.json`、`screenshots/`、`build/`、`dist/` 为运行或构建生成物，不需要提交
