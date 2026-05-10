@echo off
chcp 65001
echo 正在安装/更新打包依赖...
python -m pip install pyinstaller requests pygame

echo.
echo 正在打包 "KOReader Page Turner"...
echo 包含图标: logo.ico
echo 包含按钮图标资源: assets\icons
echo.

python -m PyInstaller --noconsole --onefile --clean --name "KOReader Page Turner" --icon "logo.ico" --add-data "logo.ico;." --add-data "logo.png;." --add-data "assets;assets" "koreader_page_turner.py"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo 打包失败！请检查错误信息。
    pause
    exit /b
)

echo.
echo ==========================================
echo 打包成功！
echo 可执行文件位于: dist\KOReader Page Turner.exe
echo ==========================================
pause
