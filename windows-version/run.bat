@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

rem ===== 配置 =====
set "APP=koreader_page_turner.py"
rem CONSOLE=1 显示控制台（能看到状态和报错）；改成 0 则无窗口静默启动
set "CONSOLE=1"
rem ==================

if not exist "%APP%" (
    echo [错误] 找不到 %APP%，请在 windows-version 目录下运行本脚本。
    pause
    exit /b 1
)

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
    echo [错误] 没找到带 tkinter 的 Python。
    echo        请安装 Python 3 官方版（安装时保留 tcl/tk 组件）后重试。
    pause
    exit /b 1
)
echo 使用 Python: %PY%

rem ---- 依赖检查 ----
"%PY%" -c "import requests" >nul 2>nul
if errorlevel 1 (
    echo 缺少 requests，正在安装...
    "%PY%" -m pip install -i https://pypi.tuna.tsinghua.edu.cn/simple requests
    if errorlevel 1 goto :fail
)

"%PY%" -c "import pygame" >nul 2>nul
if errorlevel 1 echo [提示] 未安装 pygame，手柄功能将禁用（可选："%PY%" -m pip install pygame）

rem ---- 启动 ----
set "PYW=%PY:python.exe=pythonw.exe%"
echo 启动 %APP% ...
if "%CONSOLE%"=="1" (
    "%PY%" "%APP%"
    if errorlevel 1 goto :fail
    exit /b 0
)

start "" "%PYW%" "%APP%"
exit /b 0

:fail
echo.
echo [错误] 启动失败，请查看上方输出。
pause
exit /b 1
