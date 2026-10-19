@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

rem ===== 配置 =====
set "APP=koreader_page_turner.py"
set "NAME=KOReader Page Turner"
rem ==================

rem ---- 找一个带 tkinter 的 Python：PATH 里的 python 可能缺 tcl/tk ----
set "PY="
for %%C in (
    "python.exe"
    "C:\Python313\python.exe"
    "C:\Python312\python.exe"
    "C:\Python311\python.exe"
    "C:\Python310\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python313\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
) do (
    if not defined PY (
        "%%~C" -c "import tkinter" >nul 2>nul && set "PY=%%~C"
    )
)

if not defined PY (
    echo [错误] 没找到带 tkinter 的 Python，无法打包。
    echo        请安装 Python 3 官方版（安装时保留 tcl/tk 组件）后重试。
    pause
    exit /b 1
)
echo 使用 Python: %PY%

echo.
echo 正在安装/更新打包依赖...
rem 若访问 pypi.org 慢，可把 --index-url 换成 https://pypi.tuna.tsinghua.edu.cn/simple
"%PY%" -m pip install --disable-pip-version-check --index-url https://pypi.org/simple pyinstaller pygame requests
if errorlevel 1 (
    echo.
    echo [错误] 依赖安装失败，请检查网络后重试。
    pause
    exit /b 1
)

echo.
echo 正在打包 "%NAME%"...
echo 包含图标: logo.ico
echo 包含按钮图标资源: assets\icons
echo.

"%PY%" -m PyInstaller --noconsole --onefile --clean --name "%NAME%" --icon "logo.ico" --add-data "logo.ico;." --add-data "logo.png;." --add-data "assets;assets" "%APP%"

if errorlevel 1 (
    echo.
    echo 打包失败！请检查错误信息。
    pause
    exit /b 1
)

echo.
echo ==========================================
echo 打包成功
echo 输出: dist\%NAME%.exe
echo ==========================================
pause
