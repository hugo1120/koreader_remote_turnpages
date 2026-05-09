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
