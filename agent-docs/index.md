# agent-docs 索引

## 文档列表

- [mobile-migration.md](D:/github/koreader_remote_turnpages/agent-docs/mobile-migration.md)
  适用场景：历史迁移记录；其中旧 `android-app/` 路径已被当前 `android-version/` 目录替代。
- [android-implementation-plan.md](D:/github/koreader_remote_turnpages/agent-docs/android-implementation-plan.md)
  适用场景：历史实现计划；当前工程入口以根 README 和本索引的关键记忆为准。

## 全局关键记忆

- 当前仓库按版本目录隔离：`windows-version/` 是 Windows 桌面版入口，`android-version/` 是 Android 原生版入口。
- Windows 桌面版主程序为 [windows-version/koreader_page_turner.py](D:/github/koreader_remote_turnpages/windows-version/koreader_page_turner.py)，技术栈为 Tkinter + requests + 可选 pygame。
- Android GitHub Actions 有两个入口：根目录 [.github/workflows/android-build.yml](D:/github/koreader_remote_turnpages/.github/workflows/android-build.yml) 用于同步整个仓库时构建 APK；[android-version/.github/workflows/android-build.yml](D:/github/koreader_remote_turnpages/android-version/.github/workflows/android-build.yml) 用于只同步 Android 目录成独立仓库时构建 APK。
- 2026-05-09 删除旧混放结构：根目录旧 Windows 源码、旧 `android-app/`、本地 Gradle 缓存、运行配置、备份和参考图均不再保留。
- Android 端技术栈确定为 Kotlin + Jetpack Compose。
- Android 首版目标是在移动端覆盖桌面版核心远程控制能力，并新增音量键翻页与 GitHub Actions 自动产出 APK。
