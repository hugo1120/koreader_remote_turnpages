# KOReader Remote Android

## 本地运行
1. 用 Android Studio 打开 android-app/
2. 连接 Android 设备或模拟器
3. 运行 app 模块

## 本地构建
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat assembleDebug

## GitHub Actions
- 工作流文件：仓库根目录 `.github/workflows/android-build.yml`
- 触发条件：
  - debug：`push` 到 `main`、`pull_request`、`workflow_dispatch`
  - release：`push` tag `v*`（如 `v1.0.0`）或手动触发 `workflow_dispatch` 且勾选 `build_release`
- 实际构建目录：`android-app/`
- debug 工作流会执行 `testDebugUnitTest` 与 `assembleDebug`，成功后上传 debug APK artifact `koreader-remote-debug-apk`（`android-app/app/build/outputs/apk/debug/*.apk`）
- release 工作流会先执行 `testDebugUnitTest`，再执行 `assembleDebug`，再执行 `assembleRelease`，成功后上传 release APK artifact `koreader-remote-release-apk`（`android-app/app/build/outputs/apk/release/*.apk`）

## Release Signing（GitHub Secrets）
release 构建依赖以下 Secrets：
- `ANDROID_KEYSTORE_BASE64`：keystore 文件的 Base64 文本
- `ANDROID_KEYSTORE_PASSWORD`：keystore 密码
- `ANDROID_KEY_ALIAS`：签名别名
- `ANDROID_KEY_PASSWORD`：别名对应密码

工作流会在 CI 中将 keystore 解码到 `android-app/release.keystore`，并通过以下环境变量注入 Gradle：
- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

若 `ANDROID_KEYSTORE_BASE64`、`ANDROID_KEYSTORE_PASSWORD`、`ANDROID_KEY_ALIAS`、`ANDROID_KEY_PASSWORD` 任一缺失，release job 会在解码前直接失败并中止。

## 功能范围
- KOReader 局域网控制
- 音量键翻页
- 截图保存
- GitHub Actions 产出 APK
