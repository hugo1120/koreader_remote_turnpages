# KOReader Remote Android 重构设计

## 1. 背景与现状

当前仓库只有一个桌面入口文件 [koreader_page_turner.py](D:/github/koreader_remote_turnpages/koreader_page_turner.py)。它同时承担了以下职责：

- Tkinter 桌面 UI
- KOReader HTTP 请求
- 本地配置读写
- 截图下载与保存
- 键盘、滚轮、手柄输入
- Windows 图标、标题栏和 EXE 打包配套逻辑

这种实现对 Windows 桌面工具足够直接，但不适合移动端。Android 版本无法复用 Tkinter、Windows API、PyInstaller 及 `pygame` 输入轮询，因此本次工作应视为“保留桌面版并新建 Android 客户端”，而不是“把 Python 项目直接打成 APK”。

## 2. 目标

- 在仓库内新增 `android-app/` 独立 Android 工程，不污染现有 Python 桌面源码。
- 使用 Kotlin + Jetpack Compose 开发 Android 客户端。
- 首版尽量覆盖桌面版核心控制能力：连接、上一页、下一页、旋转、全刷、截图、休眠、主题、配置持久化。
- 新增 Android 特性：音量键翻页。
- 通过 GitHub Actions 自动构建 APK，先保证 `debug APK` 可持续产出，再扩展到签名 `release APK`。

## 3. 非目标

- 不删除、不替换现有桌面版实现。
- 不在首轮迁移中处理 iOS、HarmonyOS 或桌面多端统一架构。
- 不强行复用 Python 运行时到 Android。
- 不承诺首版支持桌面端特有输入模型，如鼠标滚轮、窗口置顶、Windows 原生标题栏样式。

## 4. 需求拆解

### 4.1 可直接迁移的业务语义

桌面版已有的 KOReader 远程控制动作可以直接映射为 Android 端领域能力：

- 连接检测：`/koreader/event/GotoViewRel/0`
- 上一页：`/koreader/event/GotoViewRel/-1`
- 下一页：`/koreader/event/GotoViewRel/1`
- 旋转：`/koreader/event/SetRotationMode/{0|1}`
- 全刷：`/koreader/event/FullRefresh`
- 休眠：`/koreader/event/RequestSuspend`
- 截图：`/koreader/device/screen/bb`

### 4.2 需要按 Android 重建的能力

- 页面 UI、状态管理、生命周期处理
- 权限与文件保存
- 音量键拦截与行为配置
- 前后台切换时的连接状态与输入策略
- APK 构建、签名、CI 发布链路

## 5. 方案比较

### 方案 A：在仓库根目录直接塞入 Android 文件

做法：把 Gradle、Compose、Android 资源文件直接堆在仓库根目录，与现有 Python 文件混放。

优点：

- 起步快
- 目录层级少

缺点：

- 明显污染现有桌面项目结构
- Python 与 Android 资源混杂，维护成本高
- CI、文档、忽略规则会迅速变乱

结论：不推荐。

### 方案 B：新增 `android-app/` 独立 Android 工程，仓库单仓并存

做法：保留现有桌面版文件不动，在仓库下新增 `android-app/`，其中包含 Gradle Wrapper、Compose 源码、Android 资源和 GitHub 构建所需配置。

优点：

- 完整隔离桌面版与移动版源码
- 便于 GitHub Actions 精确定位 Android 构建目录
- 后续可以独立演进 Android 端，而不破坏桌面版
- 与当前仓库规模匹配，实施成本可控

缺点：

- 业务逻辑需要用 Kotlin 重写
- 两端存在一定行为同步成本

结论：推荐方案。

### 方案 C：新增 `android-app/`，同时一开始就做重型多模块架构

做法：在 Android 工程中直接拆成 `app`、`core-network`、`core-data`、`feature-remote`、`feature-settings` 等多个 Gradle 模块。

优点：

- 长期扩展性最好
- 模块边界清晰

缺点：

- 当前项目规模偏小，前期建模和构建配置成本偏高
- 会显著拉长首版交付周期

结论：不作为首版方案。可在 Android 端功能稳定后再考虑升级。

## 6. 推荐设计

采用方案 B：新增 `android-app/` 独立 Android 工程，在工程内部使用“单应用模块 + 清晰包分层”的方式组织代码。

推荐目录如下：

```text
/
├─ koreader_page_turner.py
├─ build_exe.bat
├─ README.MD
├─ agent-docs/
│  ├─ index.md
│  └─ mobile-migration.md
└─ android-app/
   ├─ .gitignore
   ├─ app/
   ├─ docs/
   ├─ gradle/
   ├─ gradlew
   ├─ gradlew.bat
   ├─ settings.gradle.kts
   ├─ build.gradle.kts
   └─ README.md
```

目录约束补充：

- Android 应用相关源码、Gradle 配置、子文档和本地忽略规则尽量都放在 `android-app/` 下。
- 根目录只保留无法规避的仓库级文件。
- GitHub Actions 工作流必须位于 `.github/workflows/`，这是 GitHub 平台约束，不能迁入 `android-app/`。

Android 工程内部建议包结构：

```text
android-app/app/src/main/java/<package>/
├─ MainActivity.kt
├─ app/
│  ├─ KOReaderApp.kt
│  └─ navigation/
├─ data/
│  ├─ network/
│  ├─ repository/
│  └─ settings/
├─ domain/
│  ├─ model/
│  └─ usecase/
├─ platform/
│  ├─ input/
│  └─ storage/
└─ ui/
   ├─ screen/
   ├─ component/
   ├─ state/
   └─ theme/
```

## 7. Android 端职责划分

### 7.1 `data/network`

职责：

- 封装 KOReader HTTP 接口
- 统一超时、错误映射、截图流式下载
- 只暴露领域动作，不向 UI 泄露 URL 拼接细节

建议：

- 使用 `OkHttp` 即可，当前接口简单，不强制引入 Retrofit
- 截图请求沿用桌面版思路，使用更长读取超时和流式保存，避免大图或渲染慢时失败

### 7.2 `data/settings`

职责：

- 保存最近连接 IP
- 保存主题设置
- 保存音量键翻页开关与方向映射
- 保存截图命名与保存策略等轻量配置

建议：

- 使用 `DataStore`

### 7.3 `domain`

职责：

- 统一定义动作语义，如 `NextPage`、`PreviousPage`、`RotateScreen`
- 聚合状态，如连接状态、正在截图、最近一次错误信息

建议：

- 让 ViewModel 通过 UseCase 调用 Repository，而不是直接拼 URL

### 7.4 `platform/input`

职责：

- 处理音量键、后续可扩展蓝牙键盘或手柄 KeyEvent

建议：

- 首版只对音量键做稳定支持
- 外设键盘/手柄预留扩展点，但不阻塞首版上线

### 7.5 `ui`

职责：

- Compose 页面与组件
- 页面状态展示
- 错误提示、加载态、按钮禁用态

建议页面：

- 连接页
- 控制页
- 设置页

## 8. 功能设计

### 8.1 连接页

职责：

- 输入设备 IP
- 触发连接检测
- 展示最近连接 IP

行为：

- 点击连接后发送健康检查请求
- 成功后进入控制页
- 失败时展示清晰错误信息

### 8.2 控制页

首版保留的主操作：

- 上一页
- 下一页
- 旋转
- 全刷
- 截图
- 休眠
- 断开连接

界面建议：

- 主区域保留大号上一页 / 下一页按钮
- 次操作放为工具栏或卡片按钮
- 底部或顶部显示连接目标与状态反馈

### 8.3 设置页

首版至少包含：

- 深色 / 浅色主题
- 音量键翻页总开关
- 音量键方向映射
- 截图保存说明
- 关于页或版本信息入口

## 9. 音量键翻页设计

音量键属于 Android 端新增能力，建议按“可配置、前台生效、默认安全”的原则实现。

推荐规则：

- 仅在 App 位于前台且用户已启用“音量键翻页”时拦截
- 默认映射：
  - `Volume Up -> 上一页`
  - `Volume Down -> 下一页`
- 设置页支持反转方向
- 用户关闭该功能后，系统音量键恢复默认行为

实现建议：

- 在 `MainActivity` 或承载控制页的输入宿主层统一处理 `onKeyDown` / `onKeyUp`
- 将事件交给 ViewModel 判断当前连接状态与设置状态
- 只在满足条件时消费事件

风险提示：

- 不同 ROM 的按键分发行为可能略有差异
- 如果用户有频繁调节音量的需求，必须保留总开关
- 不建议在后台服务态持续劫持音量键

## 10. 截图能力设计

桌面版截图逻辑已经暴露出一个事实：KOReader 生成截图时可能较慢，且网络读取可能中断。因此 Android 端不能把截图当作普通短请求处理。

建议：

- 对截图接口单独配置较长读取超时
- 采用流式写入，避免一次性将大图全部读入内存
- 成功后保存到系统媒体库或应用专属目录，并给出保存结果提示
- 失败时区分超时、连接中断、HTTP 错误，避免只显示笼统失败

## 11. GitHub Actions 产 APK 设计

目标分两阶段：

### 阶段 1：稳定产出 `debug APK`

工作流建议：

- 文件：`.github/workflows/android-build.yml`
- 触发：`push`、`pull_request`、`workflow_dispatch`
- 核心步骤：
  - `actions/checkout`
  - `actions/setup-java@v4`，JDK 17
  - `gradle/actions/setup-gradle@v4`
  - 在 `android-app/` 下执行 `./gradlew assembleDebug`
  - 上传 `app/build/outputs/apk/debug/*.apk` 作为 artifact

### 阶段 2：产出签名 `release APK`

在阶段 1 稳定后再增加：

- GitHub Secrets:
  - `ANDROID_KEYSTORE_BASE64`
  - `ANDROID_KEYSTORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`
- 在 workflow 中解码 keystore 并执行 `assembleRelease`
- 对 tag 或 release 事件上传签名 APK

推荐顺序：

- 先把构建链路跑通，再处理签名和发布

## 12. 测试与验证策略

### 12.1 自动化验证

- 单元测试：
  - KOReader endpoint 构造
  - 设置读写
  - 连接状态流转
  - 音量键映射逻辑
- 可能的 UI / Instrumentation 测试：
  - 连接页输入与错误态
  - 控制页按钮触发逻辑

### 12.2 手工验证

至少覆盖以下场景：

- 冷启动进入连接页
- 输入 IP 并成功连接
- 上一页 / 下一页
- 旋转、全刷、休眠
- 截图成功与失败路径
- 深浅色主题切换
- 音量键翻页开关开启 / 关闭
- 方向映射反转是否生效
- 前后台切换后按键行为是否符合预期

## 13. 分阶段实施建议

### 阶段 1：工程落地

- 新建 `android-app/`
- 建立 Gradle Wrapper、Compose、基础包结构
- 接入应用图标、应用名、最小 SDK、版本信息

### 阶段 2：核心链路

- 实现 KOReader 网络层
- 实现连接页、控制页、设置页
- 实现连接检测与核心控制动作

### 阶段 3：移动端增强

- 接入 `DataStore`
- 接入音量键翻页
- 接入截图下载与保存
- 补足主题与错误提示

### 阶段 4：CI/CD

- 配置 GitHub Actions 构建 `debug APK`
- 产出 artifact
- 稳定后增加 `release APK` 签名流程

## 14. 主要风险与应对

### 风险 1：KOReader 接口在设备或版本间存在差异

应对：

- 先以当前桌面版已验证接口为准
- 将 endpoint 常量集中管理，避免散落在 UI 层

### 风险 2：截图接口耗时高且易中断

应对：

- 独立截图请求配置
- 流式下载与分层错误提示

### 风险 3：音量键劫持影响用户正常调音量

应对：

- 默认关闭或首次明确告知
- 设置页提供总开关和方向映射
- 仅在前台生效

### 风险 4：Android 端代码膨胀后难维护

应对：

- 首版即建立清晰包分层
- 不把网络、设置、输入和 Compose 页面揉在一个文件里

## 15. 结论

最合适的实施路径是：

1. 保留现有 Python 桌面版不动。
2. 在仓库下新增 `android-app/` 独立 Android 原生工程。
3. 用 Kotlin + Jetpack Compose 重新实现移动端 UI 和输入层。
4. 复用现有 KOReader HTTP 接口语义，集中封装为 Android 网络层。
5. 将音量键翻页作为 Android 特性实现为可配置、前台生效的输入能力。
6. 先通过 GitHub Actions 稳定产出 `debug APK`，再扩展到签名 `release APK`。
