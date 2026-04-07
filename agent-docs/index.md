# agent-docs 索引

## 文档列表

- [mobile-migration.md](D:/github/koreader_remote_turnpages/agent-docs/mobile-migration.md)
  适用场景：评估并实施从 Windows Tkinter 桌面版迁移到 Android 原生 App、规划目录隔离、定义 GitHub APK 构建流程。
- [android-implementation-plan.md](D:/github/koreader_remote_turnpages/agent-docs/android-implementation-plan.md)
  适用场景：按任务拆解 Android 原生 App 重构、音量键翻页实现与 GitHub Actions 构建落地。

## 全局关键记忆

- 当前仓库现有应用入口为单文件 [koreader_page_turner.py](D:/github/koreader_remote_turnpages/koreader_page_turner.py)，技术栈为 Tkinter + requests + 可选 pygame，仅面向 Windows 桌面环境。
- Android 重构采用新增 `android-app/` 独立目录并存方案，保留现有桌面版源码和打包脚本，不在首轮迁移中污染或替换 Python 实现。
- Android 端技术栈确定为 Kotlin + Jetpack Compose。
- Android 首版目标是在移动端覆盖桌面版核心远程控制能力，并新增音量键翻页与 GitHub Actions 自动产出 APK。
