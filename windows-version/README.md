# KOReader Remote Windows

Windows 桌面版 KOReader 局域网遥控器，基于 Tkinter + requests 开发，可选 pygame 支持手柄控制。

## 功能

- 上一页 / 下一页
- 键盘快捷键：`Left`、`Right`、`Up`、`Down`、`PageUp`、`PageDown`
- 鼠标滚轮翻页
- 旋转、全刷、截图、休眠
- 浅色 / 深色主题
- 窗口置顶
- 可选 pygame 手柄支持
- 本地配置保存到 `koreader_config.json`
- 支持 PyInstaller 打包单文件 EXE

## 运行

1. 在 KOReader 中开启 HTTP 服务：`Network -> SSH/HTTP Server`
2. 安装依赖：

```powershell
python -m pip install requests
python -m pip install pygame
```

`pygame` 是可选依赖，缺失时只会禁用手柄功能。

3. 启动桌面版：

```powershell
python "koreader_page_turner.py"
```

## 打包

```powershell
./build_exe.bat
```

输出文件位于：

```text
dist/KOReader Page Turner.exe
```

## 文件说明

- `koreader_page_turner.py`：桌面版应用入口
- `build_exe.bat`：Windows EXE 打包脚本
- `logo.ico` / `logo.png`：窗口和打包图标
- `koreader_config.json`、`screenshots/`、`build/`、`dist/` 为运行或构建生成物，不需要提交
