# Repository Guidelines

## Project Structure & Module Organization
- `koreader_page_turner.py` is the only application entry point. It contains the Tkinter UI, KOReader HTTP calls, config loading, screenshot saving, and optional gamepad support.
- `build_exe.bat` packages the Windows release with PyInstaller.
- `logo.ico` and `logo.png` are the app icons used at runtime and during packaging.
- Runtime files such as `koreader_config.json`, `screenshots/`, `build/`, and `dist/` are generated locally and must not be committed.

## Build, Test, and Development Commands
- `python -m pip install requests pygame pyinstaller`: install runtime and packaging dependencies. `pygame` is optional at runtime, but required if you want gamepad support.
- `python "koreader_page_turner.py"`: run the desktop app locally.
- `python -m py_compile "koreader_page_turner.py"`: quick syntax validation before packaging or pushing.
- `build_exe.bat`: build the single-file Windows executable.
- `python -m PyInstaller --noconsole --onefile --clean --name "KOReader Page Turner" --icon "logo.ico" --add-data "logo.ico;." --add-data "logo.png;." "koreader_page_turner.py"`: manual packaging command.

## Coding Style & Naming Conventions
- Follow PEP 8 with 4-space indentation.
- Use `snake_case` for functions and variables, `PascalCase` for classes, and `UPPER_SNAKE_CASE` for constants such as `DEFAULT_GAMEPAD_MAPPING`.
- Prefer small, local changes over broad rewrites. Reuse existing UI patterns and request helpers instead of adding parallel implementations.
- Keep comments short and only explain platform quirks, key flows, or non-obvious logic.

## Testing Guidelines
- There is no automated test suite yet. Minimum validation is manual smoke testing plus `py_compile`.
- Before submitting changes, verify startup, KOReader connection, previous/next page actions, theme switching, and config persistence.
- If you change screenshot or gamepad logic, test those paths explicitly. Put future automated tests under `tests/`.

## Commit & Pull Request Guidelines
- Prefer `type: summary` commit messages, for example `fix: 修复暗色标题栏和窗口图标`.
- Keep each commit focused. Do not mix source changes with generated files or personal config.
- PRs should include the purpose, key changes, manual verification steps, and screenshots for UI changes.

## Security & Configuration Tips
- Do not commit real device IPs, local screenshots, or generated config files.
- This tool assumes a LAN-accessible KOReader HTTP server. Preserve backward compatibility when adjusting endpoints or request timing.
