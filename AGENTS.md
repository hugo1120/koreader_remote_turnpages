# AGENTS.md

## 项目现状

- 当前仓库按版本目录隔离：
  - `windows-version/`：Windows 桌面版，入口为 `koreader_page_turner.py`。
  - `android-version/`：Android 原生版，入口为 Gradle 工程。
- 旧根目录 Windows 源码和旧 `android-app/` 已移除，不要再按旧路径新增代码。
- `agent-docs/index.md` 是项目记忆入口；历史设计文档中的旧路径以索引和文档顶部状态说明为准。

## 工作约束

- 与用户沟通、文档、代码注释默认中文；保留必要英文术语。
- 修改文件优先使用 `apply_patch`。
- 内容搜索优先使用 `rg`。
- 不主动执行提交、推送、分支操作。
- 删除/移动文件或目录、大范围批量修改、`git push`、`git reset --hard`、环境/权限/系统设置变更前必须先确认。

## 常用验证

Windows 版：

```powershell
Set-Location "D:/github/koreader_remote_turnpages/windows-version"
python -m unittest "tests/test_http_helpers.py"
python -m unittest "tests/test_keyboard_mapping.py"
python -m py_compile "koreader_page_turner.py"
python -m PyInstaller --noconsole --onefile --clean --name "KOReader Page Turner" --icon "logo.ico" --add-data "logo.ico;." --add-data "logo.png;." "koreader_page_turner.py"
```

Android 版：

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-version"
./gradlew.bat testDebugUnitTest assembleDebug
```

Android APK 通常由 GitHub Actions 生成；根目录 `.github/workflows/android-build.yml` 构建 `android-version/`。

## Windows 快捷键记忆

- 快捷键只在 Windows 程序窗口获得焦点时生效，不是全局热键。
- 默认映射：`F5` 全刷，`F6` 旋转，`F7` 截图，`Esc` 休眠。
- `koreader_config.json` 的 `keyboard_mapping` 可自定义快捷键；源码运行时配置在 `windows-version/`，EXE 运行时配置在 EXE 同目录。
