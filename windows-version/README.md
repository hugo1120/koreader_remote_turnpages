# KOReader Remote Windows

Windows 桌面版 KOReader 局域网遥控器，基于 Tkinter + requests 开发，可选 pygame 支持手柄控制。

## 功能

- 上一页 / 下一页
- 支持只填 IP 前三段（如 `192.168.2`）或前两段（如 `10.68`），自动搜索网段内的设备，Wi-Fi 重连换 IP 后不用手改
- 键盘快捷键：翻页、旋转、全刷、截图、休眠
- 鼠标滚轮翻页
- 旋转、全刷、截图、休眠
- 顶部设备动作按钮悬停提示
- Lucide 风格 PNG 按钮图标，支持浅色 / 深色主题切换
- 浅色 / 深色主题
- 窗口置顶
- 按 Windows DPI 缩放逻辑尺寸保存窗口大小，降低不同分辨率 / 缩放倍率下的布局漂移
- 窗口缩小时标题栏、图标、字体、间距和控制按钮会同步进入紧凑布局
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

窗口大小会同时保存旧的像素字段 `window_width` / `window_height` 和新的逻辑尺寸字段 `window_width_dp` / `window_height_dp`。跨不同 Windows 缩放倍率使用时，程序优先读取逻辑尺寸字段。布局比例不会单独写入 JSON；程序会根据已保存的窗口大小和当前 DPI 自动计算紧凑程度。

## 连接设备

IP 输入框支持三种写法：

| 输入 | 行为 |
| --- | --- |
| `192.168.2.15` | 直接连接这个 IP |
| `192.168.2` / `192.168.2.` / `192.168.2.x` / `192.168.2.*` / `192.168.2.0` | 当成网段，自动搜索 `192.168.2.1-254` 找设备 |
| `10.68` | 只有两段时按本机网卡地址补全第三段，例如本机是 `10.68.3.7` 就搜 `10.68.3.1-254` |

自动搜索的过程：

1. 对网段内 **每一个** 地址都发一次 `/koreader/event/GotoViewRel/0`，该请求不会翻页，返回 200 就认定是 KOReader。
2. 不再做 TCP 端口预筛：装了 Clash/V2Ray 之类的代理或 TUN 网卡时，任意地址的 `8080` 都能「连上」，预筛会把真设备挤出候选列表。
3. 对设备的请求统一走禁用环境代理的会话（`trust_env=False`），避免 `HTTP_PROXY` 把 `192.168.x.x` 也转发给代理导致全部失败。
4. 多个命中时按 IP 数字序取最小的；254 个地址全扫一轮约 6 秒，状态栏实时显示 `搜索 128/254 命中 1`。

填完整 IP 但连不上时，程序会询问是否改为搜索整个网段。输入框保留的是你填的原始内容（网段形式就存网段），所以下次打开直接回车即可重新搜索。

## 运行

1. 在 KOReader 中开启 HTTP 服务：`Network -> SSH/HTTP Server`
2. 安装依赖：

```powershell
python -m pip install requests
python -m pip install pygame
```

`pygame` 是可选依赖，缺失时只会禁用手柄功能。

3. 启动桌面版：

直接双击 `run.bat`，或在命令行执行：

```powershell
./run.bat
```

`run.bat` 会自动挑一个带 tkinter 的 Python（PATH 里的 `python` 可能缺 tcl/tk 组件），缺 `requests` 时自动安装，`pygame` 缺失只提示不阻塞。想改启动行为就改脚本顶部的配置块：`CONSOLE=1` 保留控制台窗口，改成 `0` 用 `pythonw` 静默启动。

也可以手动运行：

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
python -m unittest "tests/test_ui_scaling_and_icons.py"
python -m unittest "tests/test_ip_scanning.py"
python -m py_compile "koreader_page_turner.py"
```

普通控制命令会检查 KOReader HTTP 响应状态；未连接时设备动作会在状态栏提示“请先连接设备”。截图下载复用当前连接的 `base_url`，不再单独拼接固定地址。

## 文件说明

- `koreader_page_turner.py`：桌面版应用入口
- `run.bat`：源码运行脚本，自动挑选带 tkinter 的 Python 并补齐依赖
- `tests/test_ip_scanning.py`：IP 输入解析与网段自动搜索的单元测试（扫描部分已 mock，不依赖真实设备）
- `build_exe.bat`：Windows EXE 打包脚本
- `logo.ico` / `logo.png`：窗口和打包图标
- `assets/icons/`：按钮 PNG 图标资源，打包时通过 `--add-data "assets;assets"` 写入 EXE
- `koreader_config.json`、`screenshots/`、`build/`、`dist/` 为运行或构建生成物，不需要提交
