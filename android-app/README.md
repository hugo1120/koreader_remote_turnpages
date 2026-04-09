# KOReader Remote Android

Native Android remote for KOReader, built with Kotlin + Jetpack Compose.

[Back To Root README](../README.MD) | [English](#english) | [中文](#中文)

---

## English

### Overview

`KOReader Remote Android` is the mobile client in this repository. It is designed for phones and focuses on fast page turning over the local network.

The Android app works with KOReader's HTTP server and provides a touch-friendly remote UI with remembered connection settings and mobile-specific control modes.

### Features

- Previous / next page actions
- Button mode for direct tapping
- Blind gesture mode for slide-based control
- Volume-key page turning
- Rotate, full refresh, screenshot, suspend
- Light / dark theme toggle
- Remembered host and port
- Recent host history
- Preferred subnet prefix memory for faster LAN reconnect
- GitHub Actions APK build pipeline

### Control Modes

- Button mode:
  Main touch UI with large action buttons
- Blind gesture mode:
  Swipe right or down for next page, swipe left or up for previous page

### Requirements

- Android 8.0 or newer
- KOReader with HTTP Server enabled

Enable KOReader HTTP server on the device:

```text
Network -> SSH/HTTP Server
```

### Local Run

1. Open `android-app/` in Android Studio
2. Connect an Android device or start an emulator
3. Run the `app` module

### Local Build

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat assembleDebug
```

### GitHub Actions APK Build

Workflow file:

- [../.github/workflows/android-build.yml](../.github/workflows/android-build.yml)

Triggers:

- Debug build:
  `push` to `main`, `pull_request`, or `workflow_dispatch`
- Release build:
  tag push `v*` such as `v1.0.0`, or `workflow_dispatch` with `build_release = true`

Artifacts:

- Debug APK artifact:
  `koreader-remote-debug-apk`
- Release APK artifact:
  `koreader-remote-release-apk`

Build steps:

- Debug job runs `testDebugUnitTest` and `assembleDebug`
- Release job runs `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`

### Install Notes

- GitHub Actions provides artifacts as compressed packages. Extract the artifact first, then install the `.apk` file inside.
- If an older app with the same package name is already installed and signed differently, uninstall it before installing a CI debug APK.
- If Android stays on "Installing..." for too long, check:
  - you opened the actual `.apk`, not the artifact archive
  - the old package was removed
  - "Install unknown apps" permission is enabled

### Release Signing

Release builds require these GitHub Secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The workflow decodes the keystore to `android-app/release.keystore` and passes these environment variables into Gradle:

- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

If any required signing secret is missing, the release job fails before decoding the keystore.

---

## 中文

### 概览

`KOReader Remote Android` 是这个仓库里的手机端客户端，使用 Kotlin + Jetpack Compose 开发，重点面向手机上的快速翻页与局域网控制场景。

它通过 KOReader 的 HTTP Server 与设备通信，提供更适合手机操作的遥控界面，并支持连接信息记忆与移动端控制模式切换。

### 功能

- 上一页 / 下一页
- 按钮模式
- 盲操滑动模式
- 音量键翻页
- 旋转、全刷、截图、休眠
- 深浅主题切换
- 记忆主机地址与端口
- 最近连接记录
- 记忆常用网段前缀，便于局域网重连
- GitHub Actions 自动构建 APK

### 控制模式

- 按钮模式：
  通过大按钮直接控制翻页和常用动作
- 盲操模式：
  向右或向下滑动为下一页，向左或向上滑动为上一页

### 环境要求

- Android 8.0 及以上
- 已开启 HTTP Server 的 KOReader 设备

在 KOReader 中开启 HTTP 服务：

```text
Network -> SSH/HTTP Server
```

### 本地运行

1. 使用 Android Studio 打开 `android-app/`
2. 连接 Android 真机或启动模拟器
3. 运行 `app` 模块

### 本地构建

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat assembleDebug
```

### GitHub Actions 构建 APK

工作流文件：

- [../.github/workflows/android-build.yml](../.github/workflows/android-build.yml)

触发条件：

- Debug 构建：
  `push` 到 `main`、`pull_request`、`workflow_dispatch`
- Release 构建：
  推送 `v*` 标签，例如 `v1.0.0`，或者手动触发 `workflow_dispatch` 并设置 `build_release = true`

构建产物：

- Debug APK artifact：
  `koreader-remote-debug-apk`
- Release APK artifact：
  `koreader-remote-release-apk`

构建步骤：

- Debug job 执行 `testDebugUnitTest` 与 `assembleDebug`
- Release job 执行 `testDebugUnitTest`、`assembleDebug`、`assembleRelease`

### 安装说明

- GitHub Actions 下载下来的是 artifact 压缩包，先解压，再安装里面的 `.apk`
- 如果手机里已经安装过同包名但不同签名的旧版本，安装新的 CI Debug APK 前建议先卸载旧包
- 如果手机长时间停留在“正在安装”，优先检查：
  - 打开的是否是真正的 `.apk` 文件，而不是 artifact 压缩包
  - 旧包是否已经卸载
  - 是否允许“安装未知应用”

### Release 签名

Release 构建依赖以下 GitHub Secrets：

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

工作流会把 keystore 解码到 `android-app/release.keystore`，并通过以下环境变量传给 Gradle：

- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

只要有任一签名信息缺失，Release job 会在解码前直接失败。
