# KOReader Remote

KOReader 局域网遥控工具，当前只保留两个清晰入口：

- `windows-version/`：Windows 桌面版，Tkinter + requests，可选 pygame 手柄支持。
- `android-version/`：Android 原生版，Kotlin + Jetpack Compose，通过 GitHub Actions 生成 APK。

## 下载

到 [Releases](https://github.com/hugo1120/koreader_remote_turnpages/releases/latest) 页面下载：

- `KOReader.Page.Turner.zip`：解压后得到 `KOReader Page Turner.exe`，Windows 免安装单文件版，双击即可运行
- `koreader-remote.apk`：Android 版 APK（v1.2.0 起提供）

## Windows 版

启动方式，任选一种：

- 双击 `windows-version/run.bat`：自动挑选带 tkinter 的 Python，缺 `requests` 时自动安装
- 解压下载的 `KOReader.Page.Turner.zip`，双击里面的 `KOReader Page Turner.exe`：免安装，无需 Python 环境

源码运行：

```powershell
Set-Location "D:/github/koreader_remote_turnpages/windows-version"
python -m pip install requests pygame
python "koreader_page_turner.py"
```

常用键盘控制在程序窗口获得焦点时生效：

| 功能 | 默认按键 |
| --- | --- |
| 上一页 | `PageUp` / `Left` / `Up` |
| 下一页 | `PageDown` / `Right` / `Down` |
| 全刷 | `F5` |
| 旋转 | `F6` |
| 截图 | `F7` |
| 休眠 | `Esc` |

快捷键可在 `koreader_config.json` 的 `keyboard_mapping` 中自定义。源码运行时该文件在 `windows-version/`；运行打包 EXE 时该文件在 EXE 同目录。

Windows 版普通控制命令会检查 KOReader HTTP 响应状态；未连接时设备动作会在状态栏提示。顶部旋转、全刷、截图按钮支持悬停提示，交互按钮使用随主题切换的 PNG 图标。窗口大小按 Windows DPI 逻辑尺寸保存，窗口缩小时标题栏、图标、字体、间距和控制按钮会同步进入紧凑布局。

连接设备时 IP 输入框支持三种写法：

- 完整地址 `192.168.2.15`：直接连接
- 只填前三段 `192.168.2`：自动扫描 `192.168.2.1-254` 找到设备，Wi-Fi 重连后 IP 变了也不用再手改最后一个数字
- 只填前两段 `10.68`：按本机网卡地址补全第三段再扫（本机 `10.68.3.7` 就搜 `10.68.3.1-254`）

也接受 `192.168.2.`、`192.168.2.x`、`192.168.2.*`、`192.168.2.0`。搜索会对网段内每个地址实发一次无副作用的 HTTP 探活请求（不做 TCP 端口预筛，装了代理/TUN 时端口探测会全部误报），并且不走系统环境代理。填完整 IP 但连不上时，程序会询问是否改为搜索整个网段。输入框会保留你填的原始内容，所以填网段后下次打开直接按回车即可重新搜索。

打包 EXE：

```powershell
./build_exe.bat
```

## Android 版

本地开发时用 Android Studio 打开：

```text
android-version/
```

GitHub 自动生成 APK：

- 当前仓库同步到 GitHub 时，根目录 workflow：`.github/workflows/android-build.yml`
- 只把 `android-version/` 单独同步成 GitHub 仓库时，使用目录内 workflow：`android-version/.github/workflows/android-build.yml`
- Debug APK 会作为 GitHub Actions artifact 上传
- Release APK 需要配置 Android 签名 Secrets，详见 `android-version/README.md`

## 目录约定

- 不再在仓库根目录混放 Windows 源码和 Android 工程。
- 运行配置、截图、构建产物、Gradle 本地缓存不提交。
- 旧 `android-app/` 和根目录旧 Windows 文件已移除。
