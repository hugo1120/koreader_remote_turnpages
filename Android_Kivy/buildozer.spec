[app]

# (str) Title of your application
title = KOReaderRemote

# (str) Package name
package.name = koreaderremote

# (str) Package domain (needed for android/ios packaging)
package.domain = org.hugo

# (str) Source code where the main.py live
source.dir = .

# (str) Source files to include (let empty to include all the files)
source.include_exts = py,png,jpg,kv,atlas,json

# (str) Version of your application
version = 1.0

# (list) Application requirements
# 使用最新稳定版 kivy，添加依赖的 recipe
requirements = python3,kivy,requests,urllib3,idna,certifi,charset-normalizer

# (str) Presplash of the application
#presplash.filename = %(source.dir)s/data/presplash.png

# (str) Icon of the application
#icon.filename = %(source.dir)s/data/icon.png

# (str) Supported orientation (one of landscape, sensorLandscape, portrait or all)
orientation = portrait

#
# Android specific
#

# (bool) Indicate if the application should be fullscreen or not
fullscreen = 0

# (list) Permissions
android.permissions = INTERNET

# (int) Target Android API, should be as high as possible
android.api = 33

# (int) Minimum API your APK will support.
android.minapi = 21

# (str) Android NDK version to use
android.ndk = 25b

# (bool) Use --private data storage (True) or --dir public storage (False)
android.private_storage = True

# (bool) Accept SDK license
android.accept_sdk_license = True

# (list) The Android archs to build for
android.archs = arm64-v8a

# (bool) Skip trying to update the Android SDK (buildozer 会自动下载)
android.skip_update = False

# (bool) If True, then allow the APK to request legacy external storage on Android 10+
android.allow_backup = True

[buildozer]

# (int) Log level (0 = error only, 1 = info, 2 = debug (with command output))
log_level = 2

# (int) Display warning if buildozer is run as root (0 = False, 1 = True)
warn_on_root = 0
