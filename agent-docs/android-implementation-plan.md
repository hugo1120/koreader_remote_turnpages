# KOReader Android App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保留现有 Windows 桌面版源码不变的前提下，新建 `android-app/` 原生 Android 工程，完成 KOReader 移动端远程控制 App，支持音量键翻页，并通过 GitHub Actions 自动产出 APK。

**Architecture:** Android 端采用 `Kotlin + Jetpack Compose + ViewModel + StateFlow + OkHttp + DataStore`。仓库内保留现有 Python 桌面版，Android 作为独立子工程并存；应用内部采用单 `app` 模块 + 清晰包分层，避免在首版过度模块化。

**Tech Stack:** Kotlin, Jetpack Compose, Android ViewModel, StateFlow, Kotlin Coroutines, OkHttp, DataStore Preferences, JUnit4, MockWebServer, GitHub Actions

---

## 最新进展

- [x] Android 工程基础脚手架、KOReader 网络层、设置持久化、截图保存、GitHub Actions debug/release 构建已落地
- [x] 安装图标资源与 Manifest 图标声明已补齐
- [x] 连接失败原因已透出到 UI 状态文案，便于真机排查
- [x] 三个主页面已从顶部堆叠调整为带安全区和最大宽度约束的居中布局
- [x] 音量键默认值已改为开启，连接后默认可用
- [x] 高频翻页动作已拆出独立节流通道，不再依赖全局 `isBusy`
- [x] 首页已改为遥控器式布局，新增太阳/月亮主题切换入口、拟物化按钮和小字说明
- [x] `Remote` 首页已按 `Compact / Regular / Tall` 三档高度规则重排，目标是在 `16:9` 到 `21:9` 竖屏下首屏完整显示
- [x] 智能连接页已完成：最近连接记录、独立端口输入、默认端口记忆、网段前缀辅助与尾段补全已接通
- [x] 盲操模式已完成：按钮 / 盲操双模式切换、独立手势页、中心短提示与轻触感反馈已接通
- [x] 按钮视觉模型已统一为“整块同色 + 图标上方 + 标题居中”，去掉左右双色切割和局部异色块
- [ ] GitHub Actions 需要重新编译验证本轮 UI 与交互改动
- [ ] 真机还需回归验证：音量键翻页、连续快速点按、首页主题切换、设置同步、盲操模式、智能连接页

---

## 实施约束

- 包名固定为 `io.github.hugo1120.koreaderremote`
- Android 工程固定放在 `android-app/`
- 直接在主工作区实施，不再使用 `.worktrees/`
- Android 应用相关源码、Gradle 配置、子目录文档、忽略规则尽量放在 `android-app/`
- 唯一保留在仓库根目录的 Android 集成文件是 `.github/workflows/android-build.yml`，因为 GitHub Actions 只识别该位置
- 不修改现有 [koreader_page_turner.py](D:/github/koreader_remote_turnpages/koreader_page_turner.py) 的业务行为
- 按仓库约定，不在计划中加入提交、推送、分支操作
- 优先实现 `debug APK` 持续构建，`release` 签名留作收尾任务

## 文件结构映射

### 仓库级文件

- `D:/github/koreader_remote_turnpages/.github/workflows/android-build.yml`
  责任：GitHub Actions 自动构建 `debug APK` 并上传 artifact。该文件必须保留在仓库根目录。
- `D:/github/koreader_remote_turnpages/agent-docs/index.md`
  责任：登记本实施计划文档。
- `D:/github/koreader_remote_turnpages/agent-docs/android-implementation-plan.md`
  责任：当前实施计划。

### Android 工程文件

- `D:/github/koreader_remote_turnpages/android-app/settings.gradle.kts`
- `D:/github/koreader_remote_turnpages/android-app/build.gradle.kts`
- `D:/github/koreader_remote_turnpages/android-app/gradle.properties`
- `D:/github/koreader_remote_turnpages/android-app/.gitignore`
- `D:/github/koreader_remote_turnpages/android-app/gradle/libs.versions.toml`
- `D:/github/koreader_remote_turnpages/android-app/app/build.gradle.kts`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/AndroidManifest.xml`
- `D:/github/koreader_remote_turnpages/android-app/README.md`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/app/KOReaderApp.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/app/AppContainer.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/MainActivity.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/network/KoreaderHttpClient.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/repository/KoreaderRemoteRepository.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/settings/DataStoreSettingsRepository.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/RemoteAction.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/AppScreen.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/UserPreferences.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/platform/input/HardwareButton.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/platform/input/VolumeKeyActionResolver.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/platform/storage/ScreenshotSaver.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/MainViewModel.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/state/MainUiState.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/ConnectScreen.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/RemoteScreen.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/SettingsScreen.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/component/RemoteActionButton.kt`
- `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/...`

## Task 1: 初始化 Android 工程与仓库配套

**Files:**
- Create: `D:/github/koreader_remote_turnpages/android-app/settings.gradle.kts`
- Create: `D:/github/koreader_remote_turnpages/android-app/build.gradle.kts`
- Create: `D:/github/koreader_remote_turnpages/android-app/gradle.properties`
- Create: `D:/github/koreader_remote_turnpages/android-app/.gitignore`
- Create: `D:/github/koreader_remote_turnpages/android-app/gradle/libs.versions.toml`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/build.gradle.kts`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/AndroidManifest.xml`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/res/values/strings.xml`
- Create: `D:/github/koreader_remote_turnpages/android-app/README.md`

- [ ] **Step 1: 用 Compose 最小脚手架创建 `android-app/` 工程**

```text
Name: KOReader Remote
Package name: io.github.hugo1120.koreaderremote
Language: Kotlin
Build configuration language: Kotlin DSL
Minimum SDK: 26
Use Jetpack Compose: true
```

- [ ] **Step 2: 在 `libs.versions.toml` 与 `app/build.gradle.kts` 中补齐核心依赖**

`android-app/gradle/libs.versions.toml` 新增：

```toml
[versions]
okhttp = "4.12.0"
coroutines = "1.8.1"
datastore = "1.1.1"
lifecycle = "2.8.4"
truth = "1.4.4"

[libraries]
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "okhttp" }
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
truth = { module = "com.google.truth:truth", version.ref = "truth" }
```

`android-app/app/build.gradle.kts` 依赖段补入：

```kotlin
dependencies {
    implementation(libs.okhttp)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}
```

- [ ] **Step 3: 配置 Manifest 和 Android 子目录忽略规则**

`android-app/app/src/main/AndroidManifest.xml` 至少具备：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".app.KOReaderApp"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.KOReaderRemote">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

`android-app/.gitignore` 追加：

```gitignore
/.gradle/
/build/
/app/build/
/local.properties
*.jks
*.keystore
```

- [ ] **Step 4: 写 Android 子工程说明**

`android-app/README.md` 初版内容至少包含：

```text
# KOReader Remote Android

## 本地运行
1. 用 Android Studio 打开 android-app/
2. 连接 Android 设备或模拟器
3. 运行 app 模块

## 本地构建
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat assembleDebug

## 功能范围
- KOReader 局域网控制
- 音量键翻页
- 截图保存
- GitHub Actions 产出 APK
```

- [ ] **Step 5: 运行脚手架构建验证**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest
./gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 2: 建立 KOReader HTTP 层与远程仓储

**Files:**
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/network/KoreaderHttpClient.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/repository/KoreaderRemoteRepository.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/RemoteAction.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/data/network/KoreaderHttpClientTest.kt`

- [ ] **Step 1: 先写失败测试，锁定 endpoint 和输入归一化**

```kotlin
class KoreaderHttpClientTest {
    private val server = MockWebServer()

    @Before
    fun setUp() { server.start() }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun `ping uses goto view rel zero endpoint`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))
        val client = KoreaderHttpClient(okHttpClient = OkHttpClient(), baseUrl = server.url("/").toString())

        val result = client.ping()

        assertThat(result).isTrue()
        assertThat(server.takeRequest().path).isEqualTo("/koreader/event/GotoViewRel/0")
    }

    @Test
    fun `normalizes raw host to default koreader port`() {
        val normalized = KoreaderHttpClient.normalizeBaseUrl("192.168.1.88")
        assertThat(normalized).isEqualTo("http://192.168.1.88:8080")
    }
}
```

- [ ] **Step 2: 运行测试，确认当前为失败态**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.data.network.KoreaderHttpClientTest"
```

Expected:

```text
FAILURE: unresolved reference: KoreaderHttpClient
```

- [ ] **Step 3: 实现底层 HTTP 客户端和动作映射**

`RemoteAction.kt`：

```kotlin
enum class RemoteAction(val path: String) {
    PreviousPage("/koreader/event/GotoViewRel/-1"),
    NextPage("/koreader/event/GotoViewRel/1"),
    FullRefresh("/koreader/event/FullRefresh"),
    Suspend("/koreader/event/RequestSuspend");
}
```

`KoreaderHttpClient.kt`：

```kotlin
class KoreaderHttpClient(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String,
) {
    suspend fun ping(): Boolean = execute("/koreader/event/GotoViewRel/0")

    suspend fun send(action: RemoteAction): Boolean = execute(action.path)

    suspend fun setRotation(rotationMode: Int): Boolean =
        execute("/koreader/event/SetRotationMode/$rotationMode")

    suspend fun openScreenshotStream(): InputStream {
        val request = Request.Builder().url("$baseUrl/koreader/device/screen/bb").build()
        val response = okHttpClient.newCall(request).execute()
        check(response.isSuccessful) { "screenshot request failed: ${response.code}" }
        return response.body!!.byteStream()
    }

    private fun execute(path: String): Boolean {
        val request = Request.Builder().url("$baseUrl$path").build()
        okHttpClient.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

    companion object {
        fun normalizeBaseUrl(input: String): String {
            val trimmed = input.trim().removeSuffix("/")
            val withScheme = if ("://" in trimmed) trimmed else "http://$trimmed"
            return if (withScheme.substringAfter("://").contains(":")) withScheme else "$withScheme:8080"
        }
    }
}
```

`KoreaderRemoteRepository.kt`：

```kotlin
class KoreaderRemoteRepository(
    private val okHttpClient: OkHttpClient,
) {
    suspend fun connect(rawInput: String): Result<String> = runCatching {
        val baseUrl = KoreaderHttpClient.normalizeBaseUrl(rawInput)
        val client = KoreaderHttpClient(okHttpClient, baseUrl)
        check(client.ping()) { "connect failed" }
        baseUrl
    }

    suspend fun send(baseUrl: String, action: RemoteAction): Result<Unit> = runCatching {
        check(KoreaderHttpClient(okHttpClient, baseUrl).send(action))
        Unit
    }

    suspend fun setRotation(baseUrl: String, rotationMode: Int): Result<Unit> = runCatching {
        check(KoreaderHttpClient(okHttpClient, baseUrl).setRotation(rotationMode))
        Unit
    }

    suspend fun openScreenshotStream(baseUrl: String): InputStream =
        KoreaderHttpClient(okHttpClient, baseUrl).openScreenshotStream()
}
```

- [ ] **Step 4: 让测试转绿，并补一个动作端点测试**

```kotlin
@Test
fun `next page uses plus one endpoint`() = runTest {
    server.enqueue(MockResponse().setResponseCode(200))
    val client = KoreaderHttpClient(okHttpClient = OkHttpClient(), baseUrl = server.url("/").toString().removeSuffix("/"))

    val result = client.send(RemoteAction.NextPage)

    assertThat(result).isTrue()
    assertThat(server.takeRequest().path).isEqualTo("/koreader/event/GotoViewRel/1")
}
```

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.data.network.KoreaderHttpClientTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 3: 实现设置持久化与音量键动作解析

**Files:**
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/UserPreferences.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/platform/input/HardwareButton.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/platform/input/VolumeKeyActionResolver.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/settings/DataStoreSettingsRepository.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/platform/input/VolumeKeyActionResolverTest.kt`

- [x] **Step 1: 先写音量键映射失败测试**

```kotlin
class VolumeKeyActionResolverTest {
    private val resolver = VolumeKeyActionResolver()

    @Test
    fun `returns previous page for volume up when enabled`() {
        val action = resolver.resolve(
            button = HardwareButton.VolumeUp,
            preferences = UserPreferences(volumeKeysEnabled = true, invertVolumeKeys = false),
            isConnected = true,
        )

        assertThat(action).isEqualTo(RemoteAction.PreviousPage)
    }

    @Test
    fun `returns null when volume keys disabled`() {
        val action = resolver.resolve(
            button = HardwareButton.VolumeDown,
            preferences = UserPreferences(volumeKeysEnabled = false),
            isConnected = true,
        )

        assertThat(action).isNull()
    }
}
```

- [x] **Step 2: 运行测试，确认尚未实现**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.platform.input.VolumeKeyActionResolverTest"
```

Expected:

```text
FAILURE: unresolved reference: VolumeKeyActionResolver
```

- [x] **Step 3: 实现偏好模型、DataStore 仓储与按键解析器**

`UserPreferences.kt`：

```kotlin
data class UserPreferences(
    val lastHost: String = "",
    val darkTheme: Boolean = false,
    val volumeKeysEnabled: Boolean = false,
    val invertVolumeKeys: Boolean = false,
)
```

`HardwareButton.kt`：

```kotlin
enum class HardwareButton {
    VolumeUp,
    VolumeDown,
}
```

`VolumeKeyActionResolver.kt`：

```kotlin
class VolumeKeyActionResolver {
    fun resolve(
        button: HardwareButton,
        preferences: UserPreferences,
        isConnected: Boolean,
    ): RemoteAction? {
        if (!preferences.volumeKeysEnabled || !isConnected) return null

        val normal = when (button) {
            HardwareButton.VolumeUp -> RemoteAction.PreviousPage
            HardwareButton.VolumeDown -> RemoteAction.NextPage
        }

        return if (preferences.invertVolumeKeys) {
            if (normal == RemoteAction.PreviousPage) RemoteAction.NextPage else RemoteAction.PreviousPage
        } else {
            normal
        }
    }
}
```

`DataStoreSettingsRepository.kt`：

```kotlin
class DataStoreSettingsRepository(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = "user_preferences")

    private object Keys {
        val LAST_HOST = stringPreferencesKey("last_host")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val VOLUME_KEYS_ENABLED = booleanPreferencesKey("volume_keys_enabled")
        val INVERT_VOLUME_KEYS = booleanPreferencesKey("invert_volume_keys")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            lastHost = prefs[Keys.LAST_HOST].orEmpty(),
            darkTheme = prefs[Keys.DARK_THEME] ?: false,
            volumeKeysEnabled = prefs[Keys.VOLUME_KEYS_ENABLED] ?: false,
            invertVolumeKeys = prefs[Keys.INVERT_VOLUME_KEYS] ?: false,
        )
    }

    suspend fun updateLastHost(value: String) {
        context.dataStore.edit { it[Keys.LAST_HOST] = value }
    }

    suspend fun updateDarkTheme(value: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = value }
    }

    suspend fun updateVolumeKeysEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.VOLUME_KEYS_ENABLED] = value }
    }

    suspend fun updateInvertVolumeKeys(value: Boolean) {
        context.dataStore.edit { it[Keys.INVERT_VOLUME_KEYS] = value }
    }
}
```

- [ ] **Step 4: 让解析器测试转绿，再补一个反转方向测试（已补反转测试，受 Gradle Wrapper 下载受限阻塞）**

```kotlin
@Test
fun `returns next page for volume up when inverted`() {
    val action = resolver.resolve(
        button = HardwareButton.VolumeUp,
        preferences = UserPreferences(volumeKeysEnabled = true, invertVolumeKeys = true),
        isConnected = true,
    )

    assertThat(action).isEqualTo(RemoteAction.NextPage)
}
```

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.platform.input.VolumeKeyActionResolverTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 4: 建立应用容器、ViewModel 与聚合状态

**Files:**
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/app/KOReaderApp.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/app/AppContainer.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/AppScreen.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/state/MainUiState.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/MainViewModel.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/ui/MainViewModelTest.kt`

- [ ] **Step 1: 先写 ViewModel 连接流转失败测试**

```kotlin
class MainViewModelTest {
    @Test
    fun `connect success moves app to remote screen`() = runTest {
        val viewModel = MainViewModel(
            remoteRepository = FakeRemoteRepository(connectResult = Result.success("http://192.168.1.88:8080")),
            settingsRepository = FakeSettingsRepository(),
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        viewModel.onHostChanged("192.168.1.88")
        viewModel.connect()

        assertThat(viewModel.uiState.value.isConnected).isTrue()
        assertThat(viewModel.uiState.value.currentScreen).isEqualTo(AppScreen.Remote)
        assertThat(viewModel.uiState.value.baseUrl).isEqualTo("http://192.168.1.88:8080")
    }
}
```

- [ ] **Step 2: 运行测试，确认当前缺少状态层实现**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.ui.MainViewModelTest"
```

Expected:

```text
FAILURE: unresolved reference: MainViewModel
```

- [ ] **Step 3: 实现应用容器和主状态模型**

`AppScreen.kt`：

```kotlin
enum class AppScreen {
    Connect,
    Remote,
    Settings,
}
```

`MainUiState.kt`：

```kotlin
data class MainUiState(
    val currentScreen: AppScreen = AppScreen.Connect,
    val hostInput: String = "",
    val baseUrl: String = "",
    val isConnected: Boolean = false,
    val isBusy: Boolean = false,
    val rotationMode: Int = 0,
    val statusMessage: String = "准备就绪",
    val isError: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
)
```

`KOReaderApp.kt`：

```kotlin
class KOReaderApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
```

`AppContainer.kt`：

```kotlin
class AppContainer(context: Context) {
    private val okHttpClient = OkHttpClient.Builder().build()

    val settingsRepository = DataStoreSettingsRepository(context)
    val remoteRepository = KoreaderRemoteRepository(okHttpClient)
    val screenshotSaver = ScreenshotSaver(context)
    val volumeKeyActionResolver = VolumeKeyActionResolver()
}
```

- [ ] **Step 4: 实现 `MainViewModel` 最小可用版本**

```kotlin
class MainViewModel(
    private val remoteRepository: KoreaderRemoteRepository,
    private val settingsRepository: DataStoreSettingsRepository,
    private val screenshotSaver: ScreenshotSaver,
    private val volumeKeyActionResolver: VolumeKeyActionResolver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.preferencesFlow.collect { prefs ->
                _uiState.update {
                    it.copy(
                        preferences = prefs,
                        hostInput = if (it.hostInput.isBlank()) prefs.lastHost else it.hostInput,
                    )
                }
            }
        }
    }

    fun onHostChanged(value: String) {
        _uiState.update { it.copy(hostInput = value) }
    }

    fun connect() {
        val input = uiState.value.hostInput
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, isError = false, statusMessage = "连接中...") }
            remoteRepository.connect(input)
                .onSuccess { baseUrl ->
                    settingsRepository.updateLastHost(input)
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isConnected = true,
                            currentScreen = AppScreen.Remote,
                            baseUrl = baseUrl,
                            statusMessage = "连接成功",
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isBusy = false, isError = true, statusMessage = "连接失败") }
                }
        }
    }
}
```

- [ ] **Step 5: 跑通 ViewModel 测试**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.ui.MainViewModelTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 5: 实现 Compose 页面与前台音量键拦截

**Files:**
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/MainActivity.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/ConnectScreen.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/RemoteScreen.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/SettingsScreen.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/component/RemoteActionButton.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/MainViewModel.kt`

- [ ] **Step 1: 先写 ViewModel 发送动作的失败测试**

在 `MainViewModelTest.kt` 追加：

```kotlin
@Test
fun `hardware volume down triggers next page when enabled`() = runTest {
    val remoteRepository = FakeRemoteRepository(connectResult = Result.success("http://192.168.1.88:8080"))
    val settingsRepository = FakeSettingsRepository(
        initialPreferences = UserPreferences(volumeKeysEnabled = true)
    )
    val viewModel = MainViewModel(
        remoteRepository = remoteRepository,
        settingsRepository = settingsRepository,
        screenshotSaver = FakeScreenshotSaver(),
        volumeKeyActionResolver = VolumeKeyActionResolver(),
    )

    viewModel.seedConnectedState("http://192.168.1.88:8080")
    val handled = viewModel.onHardwareButton(HardwareButton.VolumeDown)

    assertThat(handled).isTrue()
    assertThat(remoteRepository.lastAction).isEqualTo(RemoteAction.NextPage)
}
```

- [ ] **Step 2: 运行测试，确认缺少动作派发接口**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.ui.MainViewModelTest"
```

Expected:

```text
FAILURE: unresolved reference: onHardwareButton
```

- [ ] **Step 3: 在 ViewModel 中实现远程动作派发与按键处理**

向 `MainViewModel.kt` 增加：

```kotlin
fun seedConnectedState(baseUrl: String) {
    _uiState.update {
        it.copy(
            baseUrl = baseUrl,
            isConnected = true,
            currentScreen = AppScreen.Remote,
        )
    }
}

fun sendAction(action: RemoteAction) {
    val baseUrl = uiState.value.baseUrl
    if (baseUrl.isBlank()) return

    viewModelScope.launch {
        remoteRepository.send(baseUrl, action)
            .onSuccess {
                _uiState.update { it.copy(statusMessage = action.name, isError = false) }
            }
            .onFailure {
                _uiState.update { it.copy(statusMessage = "操作失败", isError = true) }
            }
    }
}

fun onHardwareButton(button: HardwareButton): Boolean {
    val action = volumeKeyActionResolver.resolve(
        button = button,
        preferences = uiState.value.preferences,
        isConnected = uiState.value.isConnected,
    ) ?: return false

    sendAction(action)
    return true
}

fun openSettings() {
    _uiState.update { it.copy(currentScreen = AppScreen.Settings) }
}

fun closeSettings() {
    _uiState.update { it.copy(currentScreen = if (it.isConnected) AppScreen.Remote else AppScreen.Connect) }
}
```

- [ ] **Step 4: 写出三个 Compose 页面和复用按钮**

`RemoteActionButton.kt`：

```kotlin
@Composable
fun RemoteActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(72.dp),
    ) {
        Text(text = text)
    }
}
```

`ConnectScreen.kt`：

```kotlin
@Composable
fun ConnectScreen(
    state: MainUiState,
    onHostChanged: (String) -> Unit,
    onConnectClick: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "连接设备", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.hostInput,
            onValueChange = onHostChanged,
            label = { Text("IP 地址或主机名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onConnectClick, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.isBusy) "连接中..." else "立即连接")
        }
        TextButton(onClick = onOpenSettings) { Text("设置") }
        Text(text = state.statusMessage, color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
```

`RemoteScreen.kt`：

```kotlin
@Composable
fun RemoteScreen(
    state: MainUiState,
    onAction: (RemoteAction) -> Unit,
    onRotate: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenSettings: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "已连接: ${state.hostInput}")
        RemoteActionButton(text = "上一页", onClick = { onAction(RemoteAction.PreviousPage) })
        RemoteActionButton(text = "下一页", onClick = { onAction(RemoteAction.NextPage) })
        RemoteActionButton(text = "旋转", onClick = onRotate)
        RemoteActionButton(text = "全刷", onClick = { onAction(RemoteAction.FullRefresh) })
        RemoteActionButton(text = "截图", onClick = onScreenshot)
        RemoteActionButton(text = "休眠", onClick = { onAction(RemoteAction.Suspend) })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) { Text("设置") }
            OutlinedButton(onClick = onDisconnect, modifier = Modifier.weight(1f)) { Text("断开") }
        }
        Text(text = state.statusMessage, color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
```

`SettingsScreen.kt`：

```kotlin
@Composable
fun SettingsScreen(
    state: MainUiState,
    onVolumeKeysEnabledChanged: (Boolean) -> Unit,
    onInvertVolumeKeysChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "设置", style = MaterialTheme.typography.headlineMedium)
        SwitchRow("启用音量键翻页", state.preferences.volumeKeysEnabled, onVolumeKeysEnabledChanged)
        SwitchRow("反转音量键方向", state.preferences.invertVolumeKeys, onInvertVolumeKeysChanged)
        SwitchRow("深色主题", state.preferences.darkTheme, onDarkThemeChanged)
        OutlinedButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) { Text("返回") }
    }
}
```

- [ ] **Step 5: 在 `MainActivity` 中接入 Compose 根宿主和音量键拦截**

```kotlin
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as KOReaderApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            KOReaderRemoteTheme(darkTheme = state.preferences.darkTheme) {
                when (state.currentScreen) {
                    AppScreen.Connect -> ConnectScreen(
                        state = state,
                        onHostChanged = viewModel::onHostChanged,
                        onConnectClick = viewModel::connect,
                        onOpenSettings = viewModel::openSettings,
                    )
                    AppScreen.Remote -> RemoteScreen(
                        state = state,
                        onAction = viewModel::sendAction,
                        onRotate = viewModel::toggleRotation,
                        onScreenshot = viewModel::takeScreenshot,
                        onOpenSettings = viewModel::openSettings,
                        onDisconnect = viewModel::disconnect,
                    )
                    AppScreen.Settings -> SettingsScreen(
                        state = state,
                        onVolumeKeysEnabledChanged = viewModel::setVolumeKeysEnabled,
                        onInvertVolumeKeysChanged = viewModel::setInvertVolumeKeys,
                        onDarkThemeChanged = viewModel::setDarkTheme,
                        onBackClick = viewModel::closeSettings,
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val handled = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> viewModel.onHardwareButton(HardwareButton.VolumeUp)
            KeyEvent.KEYCODE_VOLUME_DOWN -> viewModel.onHardwareButton(HardwareButton.VolumeDown)
            else -> false
        }
        return handled || super.onKeyDown(keyCode, event)
    }
}
```

- [ ] **Step 6: 运行单元测试并本地装起 UI**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.ui.MainViewModelTest"
./gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

## Task 6: 实现旋转、设置更新、截图保存

**Files:**
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/platform/storage/ScreenshotSaver.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/MainViewModel.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/data/network/KoreaderHttpClientTest.kt`

- [ ] **Step 1: 先补截图 endpoint 失败测试**

```kotlin
@Test
fun `screenshot uses screen bb endpoint`() {
    server.enqueue(MockResponse().setResponseCode(200).setBody("png"))
    val client = KoreaderHttpClient(okHttpClient = OkHttpClient(), baseUrl = server.url("/").toString().removeSuffix("/"))

    client.openScreenshotStream().use { it.readBytes() }

    assertThat(server.takeRequest().path).isEqualTo("/koreader/device/screen/bb")
}
```

- [ ] **Step 2: 实现截图保存器**

```kotlin
class ScreenshotSaver(private val context: Context) {
    suspend fun save(stream: InputStream): String = withContext(Dispatchers.IO) {
        val fileName = "koreader_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KOReader Remote")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("unable to create media store record")
            context.contentResolver.openOutputStream(uri)?.use { output ->
                stream.copyTo(output)
            } ?: error("unable to open output stream")
            uri.toString()
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "KOReader Remote")
            dir.mkdirs()
            val file = File(dir, fileName)
            file.outputStream().use { output -> stream.copyTo(output) }
            file.absolutePath
        }
    }
}
```

- [ ] **Step 3: 在 `MainViewModel` 中补齐旋转和设置更新逻辑**

```kotlin
fun toggleRotation() {
    val nextMode = if (uiState.value.rotationMode == 0) 1 else 0
    val baseUrl = uiState.value.baseUrl
    if (baseUrl.isBlank()) return

    viewModelScope.launch {
        remoteRepository.setRotation(baseUrl, nextMode)
            .onSuccess {
                _uiState.update { it.copy(rotationMode = nextMode, statusMessage = "旋转中...", isError = false) }
            }
            .onFailure {
                _uiState.update { it.copy(statusMessage = "旋转失败", isError = true) }
            }
    }
}

fun setDarkTheme(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.updateDarkTheme(enabled) }
}

fun setVolumeKeysEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.updateVolumeKeysEnabled(enabled) }
}

fun setInvertVolumeKeys(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.updateInvertVolumeKeys(enabled) }
}

fun disconnect() {
    _uiState.update {
        it.copy(
            currentScreen = AppScreen.Connect,
            isConnected = false,
            baseUrl = "",
            statusMessage = "已断开连接",
            isError = false,
        )
    }
}

fun takeScreenshot() {
    val baseUrl = uiState.value.baseUrl
    if (baseUrl.isBlank()) return

    viewModelScope.launch {
        runCatching {
            remoteRepository.openScreenshotStream(baseUrl).use { stream ->
                screenshotSaver.save(stream)
            }
        }.onSuccess { location ->
            _uiState.update { it.copy(statusMessage = "截图已保存: $location", isError = false) }
        }.onFailure {
            _uiState.update { it.copy(statusMessage = "截图失败", isError = true) }
        }
    }
}
```

- [ ] **Step 4: 跑通网络与截图测试**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest --tests "io.github.hugo1120.koreaderremote.data.network.KoreaderHttpClientTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5: 在真机上完成手工验证**

```text
1. 连接成功后进入控制页
2. 上一页 / 下一页可用
3. 音量键开关关闭时不拦截系统音量
4. 音量键开关开启时，Volume Up / Down 按设置映射翻页
5. 旋转、全刷、休眠可用
6. 截图保存成功，路径可见
```

## Task 7: 配置 GitHub Actions 产出 APK 并补文档

**Files:**
- Create: `D:/github/koreader_remote_turnpages/.github/workflows/android-build.yml`
- Modify: `D:/github/koreader_remote_turnpages/android-app/README.md`

- [ ] **Step 1: 编写 Android 构建工作流**

```yaml
name: android-build

on:
  push:
    branches: ["main"]
  pull_request:
  workflow_dispatch:

jobs:
  build-debug-apk:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build debug APK
        working-directory: android-app
        run: ./gradlew testDebugUnitTest assembleDebug

      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: koreader-remote-debug-apk
          path: android-app/app/build/outputs/apk/debug/*.apk
```

- [ ] **Step 2: 更新 `android-app/README.md`，说明本地构建与 CI**

在 `android-app/README.md` 增加：

```text
## GitHub Actions

GitHub Actions 工作流文件位于仓库根目录 `.github/workflows/android-build.yml`。
它会在仓库根目录触发，但实际构建目录固定为 `android-app/`。
成功后会上传 debug APK artifact。
```

- [ ] **Step 3: 执行最终验证**

Run:

```powershell
Set-Location "D:/github/koreader_remote_turnpages/android-app"
./gradlew.bat testDebugUnitTest
./gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

GitHub 验证：

```text
1. 推送包含 workflow 的分支
2. 打开 Actions 页面确认 `android-build` 执行成功
3. 下载 artifact `koreader-remote-debug-apk`
4. 在 Android 设备安装 APK 验证连接、翻页、音量键和截图
```

## Task 8: 收尾 `release APK` 签名能力

**Files:**
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/build.gradle.kts`
- Modify: `D:/github/koreader_remote_turnpages/.github/workflows/android-build.yml`
- Modify: `D:/github/koreader_remote_turnpages/android-app/README.md`

- [ ] **Step 1: 在 `app/build.gradle.kts` 预留 release 签名配置**

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = System.getenv("ANDROID_KEYSTORE_PATH")?.let(::file)
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("ANDROID_KEY_ALIAS")
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

- [ ] **Step 2: 在 workflow 中加入条件式 release 构建**

```yaml
      - name: Decode keystore
        if: github.event_name == 'workflow_dispatch' && github.ref_type == 'tag'
        working-directory: android-app
        env:
          ANDROID_KEYSTORE_BASE64: ${{ secrets.ANDROID_KEYSTORE_BASE64 }}
        run: |
          echo "$ANDROID_KEYSTORE_BASE64" | base64 -d > release.keystore

      - name: Build release APK
        if: github.event_name == 'workflow_dispatch' && github.ref_type == 'tag'
        working-directory: android-app
        env:
          ANDROID_KEYSTORE_PATH: ${{ github.workspace }}/android-app/release.keystore
          ANDROID_KEYSTORE_PASSWORD: ${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
          ANDROID_KEY_ALIAS: ${{ secrets.ANDROID_KEY_ALIAS }}
          ANDROID_KEY_PASSWORD: ${{ secrets.ANDROID_KEY_PASSWORD }}
        run: ./gradlew assembleRelease
```

- [ ] **Step 3: 文档写清 Secrets 要求**

在 `android-app/README.md` 增加：

```text
## Release 签名所需 Secrets

- ANDROID_KEYSTORE_BASE64
- ANDROID_KEYSTORE_PASSWORD
- ANDROID_KEY_ALIAS
- ANDROID_KEY_PASSWORD
```

- [ ] **Step 4: 在打 tag 的工作流中验证 release 产物**

```text
1. 设置 GitHub Secrets
2. 触发 tag 构建
3. 确认 `assembleRelease` 成功
4. 下载 release APK 并在设备安装验证
```

## Task 9: 实现智能连接页与局域网记忆

**Files:**
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/UserPreferences.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/settings/DataStoreSettingsRepository.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/MainViewModel.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/state/MainUiState.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/ConnectScreen.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/network/KoreaderHttpClient.kt`
- Test: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/data/settings/DataStoreSettingsRepositoryTest.kt`
- Test: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/ui/MainViewModelTest.kt`
- Test: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/data/network/KoreaderHttpClientTest.kt`

- [x] 扩展偏好模型，新增 `lastPort`、`recentHosts`、`preferredSubnetPrefix`
- [x] 为 DataStore 默认值与更新逻辑补单测，覆盖默认端口 `8080`、最近记录顺序和前缀恢复
- [x] 在连接输入链路中新增“尾段补全”规则：当输入仅为数字且存在最近前缀时，自动补成完整 host
- [x] 将端口拆成独立输入状态，不再只依赖地址字符串携带端口
- [x] 在连接成功后更新：
  - `lastHost`
  - `lastPort`
  - `recentHosts`
  - `preferredSubnetPrefix`
- [x] 在连接页展示最近记录区，点击后直接回填地址和端口
- [x] 在连接页展示当前常用网段提示，例如 `192.168.10.*`
- [x] 同步收口连接页按钮视觉，避免出现左右双色切割；连接页主按钮也采用统一单色风格
- [ ] 回归验证：
  - 输入完整地址 + 默认端口
  - 输入完整地址 + 自定义端口
  - 输入尾段数字时自动补全
  - 点击最近记录直接连接

## Task 10: 实现按钮 / 盲操双模式与手势翻页

**Files:**
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/domain/model/UserPreferences.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/data/settings/DataStoreSettingsRepository.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/MainViewModel.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/state/MainUiState.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/MainActivity.kt`
- Modify: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/RemoteScreen.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/ui/screen/BlindControlScreen.kt`
- Create: `D:/github/koreader_remote_turnpages/android-app/app/src/main/java/io/github/hugo1120/koreaderremote/platform/input/SwipePageTurnDetector.kt`
- Test: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/ui/MainViewModelTest.kt`
- Test: `D:/github/koreader_remote_turnpages/android-app/app/src/test/java/io/github/hugo1120/koreaderremote/platform/input/SwipePageTurnDetectorTest.kt`

- [x] 扩展偏好模型，新增“上次使用控制模式”字段
- [x] 在 ViewModel 中加入 `Button` / `Blind` 模式切换状态，并在连接成功后恢复上次模式
- [x] 新增独立的 `BlindControlScreen`，只承载手势翻页，不承载复杂工具按钮
- [x] 新增 `SwipePageTurnDetector`，统一手势判定规则：
  - 向右 / 向下 -> 下一页
  - 向左 / 向上 -> 上一页
  - 只识别单指
  - 有最小位移阈值
  - 有边缘安全区
- [x] 在按钮页顶部加入模式切换器：`按钮` / `盲操`
- [x] 同步修正按钮模式页视觉：
  - 按钮整体为单一主底色
  - 图标改为上方居中
  - 标题居中
  - 不再保留左右双色切割和左侧半张色块
- [x] 在盲操页加入轻反馈：
  - 触发后中心短提示
  - 可选轻震动
- [x] 保持音量键翻页和按钮翻页、盲操翻页共用同一高频通道，避免行为分叉
- [ ] 回归验证：
  - 从按钮模式切到盲操模式
  - 退出重进后恢复上次模式
  - 上下左右四个方向映射正确
  - 边缘区域不和系统返回手势冲突
  - 音量键与盲操模式共存时行为一致

## 自检

### 覆盖性检查

- Android 独立工程：Task 1
- KOReader HTTP 能力：Task 2
- 设置持久化与音量键翻页：Task 3、Task 5
- Compose 连接/控制/设置页：Task 5
- 旋转、全刷、休眠、截图：Task 2、Task 5、Task 6
- GitHub 自动产 APK：Task 7
- Release 签名：Task 8
- 智能连接页与局域网记忆：Task 9
- 按钮 / 盲操双模式：Task 10

### 占位项检查

- 已固定包名、路径、主要文件名、工作流文件名
- 已提供关键类骨架、测试样例、构建命令
- 未包含 `TODO`、`TBD`、`待定版本` 等占位表达

### 说明

- 任务 1 属于工程脚手架搭建，未强行添加无意义 failing test，改为用构建成功校验脚手架有效性。
- 其余核心业务任务按先测后写的顺序拆解。
