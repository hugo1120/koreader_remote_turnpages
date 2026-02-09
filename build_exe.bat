@echo off
chcp 65001
echo 正在安装/更新 PyInstaller...
pip install pyinstaller

echo.
echo 正在打包 "KOReader翻页助手"...
echo 包含图标: icon.ico
echo.

python -m PyInstaller --noconsole --onefile --clean --name "KOReader翻页助手" --icon "icon.ico" --add-data "icon.ico;." KOReader_Pro.py

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo 打包失败！请检查错误信息。
    pause
    exit /b
)

echo.
echo ==========================================
echo 打包成功！
echo 可执行文件位于: dist\KOReader翻页助手.exe
echo ==========================================
pause
