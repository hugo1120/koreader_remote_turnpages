# APK 打包教程 (使用 WSL)

注意：本教程假设您已经安装了 WSL (Ubuntu)。

## 1. 准备环境 (在 WSL 中执行)

打开您的 WSL 终端 (Ubuntu)，依次运行以下命令安装必要的依赖库：

```bash
sudo apt update
sudo apt install -y git zip unzip openjdk-17-jdk python3-pip autoconf libtool pkg-config zlib1g-dev libncurses5-dev libncursesw5-dev libtinfo5 cmake libffi-dev libssl-dev
pip3 install --user --upgrade buildozer Cython==0.29.33 virtualenv
```

> **注意**：最好将 buildozer 路径加入环境变量，或者直接使用 `~/.local/bin/buildozer`。

## 2. 拷贝代码

将 Windows 下的 `d:\code\koreader翻页\Android_Kivy` 文件夹内容复制到 WSL 中，或者直接在 WSL 访问 Windows 文件（不推荐直接在 /mnt/d 下编译，速度慢且可能有权限问题）。

建议操作：
```bash
# 在 WSL 中创建目录
mkdir -p ~/koreader_apk
cd ~/koreader_apk

# 复制 Windows 文件 (注意路径根据您实际情况调整)
cp -r /mnt/d/code/koreader翻页/Android_Kivy/* .
```

## 3. 开始打包

在 `~/koreader_apk` 目录下（确保有 `main.py` 和 `buildozer.spec`），运行：

```bash
~/.local/bin/buildozer android debug
```

- 第一次运行会下载大量依赖（Android SDK/NDK, Python, Kivy 等），可能需要 15-30 分钟，请保持网络畅通。
- 如果遇到报错，请检查报错信息的最后几行。

## 4. 获取 APK

打包成功后，APK 文件会在 `bin/` 目录下。

```bash
ls bin/
```

您可以看到类似 `koreaderremote-1.0-debug.apk` 的文件。您可以将其复制回 Windows：

```bash
cp bin/*.apk /mnt/d/code/koreader翻页/
```

然后将 APK 发送到手机安装即可。

## 功能说明
- **音量键翻页**：打开 App 连接后，按手机音量键即可翻页。
- **界面**：支持极简昼夜模式。
