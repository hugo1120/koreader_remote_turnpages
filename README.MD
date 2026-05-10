# KOReader Remote

KOReader 局域网遥控工具，当前只保留两个清晰入口：

- `windows-version/`：Windows 桌面版，Tkinter + requests，可选 pygame 手柄支持。
- `android-version/`：Android 原生版，Kotlin + Jetpack Compose，通过 GitHub Actions 生成 APK。

## Windows 版

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

Windows 版普通控制命令会检查 KOReader HTTP 响应状态；未连接时设备动作会在状态栏提示。顶部旋转、全刷、截图按钮支持悬停提示。

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
