# agent-docs 索引

## 文档列表

- [mobile-migration.md](D:/github/koreader_remote_turnpages/agent-docs/mobile-migration.md)
  适用场景：历史迁移记录；文档顶部已标注旧 `android-app/` 路径被当前 `android-version/` 目录替代。
- [android-implementation-plan.md](D:/github/koreader_remote_turnpages/agent-docs/android-implementation-plan.md)
  适用场景：历史实现计划；文档顶部已标注旧路径和旧约束过期，当前工程入口以根 README 和本索引为准。

## 全局关键记忆

- 当前仓库按版本目录隔离：`windows-version/` 是 Windows 桌面版入口，`android-version/` 是 Android 原生版入口。
- Windows 桌面版主程序为 [windows-version/koreader_page_turner.py](D:/github/koreader_remote_turnpages/windows-version/koreader_page_turner.py)，技术栈为 Tkinter + requests + 可选 pygame。
- Android GitHub Actions 有两个入口：根目录 [.github/workflows/android-build.yml](D:/github/koreader_remote_turnpages/.github/workflows/android-build.yml) 用于同步整个仓库时构建 APK；[android-version/.github/workflows/android-build.yml](D:/github/koreader_remote_turnpages/android-version/.github/workflows/android-build.yml) 用于只同步 Android 目录成独立仓库时构建 APK。
- 2026-05-09 删除旧混放结构：根目录旧 Windows 源码、旧 `android-app/`、本地 Gradle 缓存、运行配置、备份和参考图均不再保留。
- 2026-05-10 Windows 版新增可配置键盘映射：默认 `F5` 全刷、`F6` 旋转、`F7` 截图、`Esc` 休眠；快捷键只在程序窗口获得焦点时生效，配置项为 `koreader_config.json` 的 `keyboard_mapping`。
- 2026-05-10 Windows 版交互按钮改用 Lucide 风格 PNG 图标资源，资源目录为 `windows-version/assets/icons/`；打包脚本必须保留 `--add-data "assets;assets"`。
- 2026-05-10 Windows 版窗口尺寸新增 DPI 逻辑尺寸保存字段 `window_width_dp` / `window_height_dp`，用于减少不同分辨率和系统缩放倍率下的布局漂移。
- 2026-05-12 Windows 版最小窗口调整为 `220x180 dp`；紧凑布局比例不写入 JSON，程序根据已保存窗口大小与当前 DPI 动态计算，窗口缩小时标题栏、按钮图标、字号和间距同步缩小。
- Windows 版图标资源需要覆盖 `ICON_SIZE_BUCKETS`，当前包含 `16/20/24/25/30/36/40/48` 尺寸目录；新增尺寸桶时必须同步生成 PNG 并跑 `tests/test_ui_scaling_and_icons.py`。
- Windows 键盘映射测试位于 [windows-version/tests/test_keyboard_mapping.py](D:/github/koreader_remote_turnpages/windows-version/tests/test_keyboard_mapping.py)，UI/DPI/图标资源测试位于 [windows-version/tests/test_ui_scaling_and_icons.py](D:/github/koreader_remote_turnpages/windows-version/tests/test_ui_scaling_and_icons.py)；最小验证命令为 `python -m unittest "tests/test_http_helpers.py" "tests/test_keyboard_mapping.py" "tests/test_ui_scaling_and_icons.py"` 与 `python -m py_compile "koreader_page_turner.py"`。
- Android 端技术栈确定为 Kotlin + Jetpack Compose。
- Android 首版目标是在移动端覆盖桌面版核心远程控制能力，并新增音量键翻页与 GitHub Actions 自动产出 APK。
