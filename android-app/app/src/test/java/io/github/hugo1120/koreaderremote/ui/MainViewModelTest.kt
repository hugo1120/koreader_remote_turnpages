package io.github.hugo1120.koreaderremote.ui

import com.google.common.truth.Truth.assertThat
import io.github.hugo1120.koreaderremote.data.repository.KoreaderRemoteRepository
import io.github.hugo1120.koreaderremote.data.settings.SettingsRepository
import io.github.hugo1120.koreaderremote.domain.model.AppScreen
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import io.github.hugo1120.koreaderremote.domain.model.UserPreferences
import io.github.hugo1120.koreaderremote.platform.input.HardwareButton
import io.github.hugo1120.koreaderremote.platform.input.PageTurnRateLimiter
import io.github.hugo1120.koreaderremote.platform.input.VolumeKeyActionResolver
import io.github.hugo1120.koreaderremote.platform.storage.ScreenshotSaver
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.SocketTimeoutException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `connect success moves app to remote screen`() = runTest {
        val expectedBaseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(lastHost = "192.168.1.88"),
        )
        val remoteRepository = FakeRemoteRepository(
            connectBehavior = { Result.success(expectedBaseUrl) },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.connect()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.currentScreen).isEqualTo(AppScreen.Remote)
        assertThat(state.isConnected).isTrue()
        assertThat(state.baseUrl).isEqualTo(expectedBaseUrl)
        assertThat(state.statusMessage).isEqualTo("连接成功")
        assertThat(state.isError).isFalse()
        assertThat(state.isBusy).isFalse()
        assertThat(remoteRepository.lastConnectInput).isEqualTo("192.168.1.88")
        assertThat(settingsRepository.lastUpdatedHost).isEqualTo("192.168.1.88")
    }

    @Test
    fun `connect failure resets connection state and shows error`() = runTest {
        val oldBaseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(lastHost = "192.168.1.88"),
        )
        val remoteRepository = FakeRemoteRepository(
            connectBehavior = { input ->
                if (input == "192.168.1.88") {
                    Result.success(oldBaseUrl)
                } else {
                    Result.failure(IllegalStateException("connect failed"))
                }
            },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.connect()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isConnected).isTrue()
        assertThat(viewModel.uiState.value.baseUrl).isEqualTo(oldBaseUrl)

        viewModel.onHostChanged("bad-host")
        viewModel.connect()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isBusy).isFalse()
        assertThat(state.isConnected).isFalse()
        assertThat(state.currentScreen).isEqualTo(AppScreen.Connect)
        assertThat(state.baseUrl).isEmpty()
        assertThat(state.statusMessage).isEqualTo("连接失败：无法访问 KOReader 接口")
        assertThat(state.isError).isTrue()
    }

    @Test
    fun `connect failure surfaces underlying reason in status`() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository(
            connectBehavior = {
                Result.failure(SocketTimeoutException("timeout"))
            },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.onHostChanged("192.168.1.88")
        viewModel.connect()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isConnected).isFalse()
        assertThat(state.isError).isTrue()
        assertThat(state.statusMessage).contains("连接失败")
        assertThat(state.statusMessage).contains("timeout")
    }

    @Test
    fun `blank host does not call repository and shows error`() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.connect()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(remoteRepository.connectCallCount).isEqualTo(0)
        assertThat(state.isBusy).isFalse()
        assertThat(state.isConnected).isFalse()
        assertThat(state.currentScreen).isEqualTo(AppScreen.Connect)
        assertThat(state.baseUrl).isEmpty()
        assertThat(state.statusMessage).isEqualTo("连接失败")
        assertThat(state.isError).isTrue()
    }

    @Test
    fun `second connect call is ignored while busy`() = runTest {
        val connectGate = CompletableDeferred<Unit>()
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(lastHost = "192.168.1.88"),
        )
        val remoteRepository = FakeRemoteRepository(
            connectBehavior = {
                connectGate.await()
                Result.success("http://192.168.1.88:8080")
            },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.connect()
        assertThat(viewModel.uiState.value.isBusy).isTrue()
        assertThat(viewModel.uiState.value.statusMessage).isEqualTo("连接中...")

        viewModel.connect()
        advanceUntilIdle()
        assertThat(remoteRepository.connectCallCount).isEqualTo(1)

        connectGate.complete(Unit)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isBusy).isFalse()
        assertThat(viewModel.uiState.value.isConnected).isTrue()
    }

    @Test
    fun `connect success still updates ui when persisting host fails`() = runTest {
        val expectedBaseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(lastHost = "192.168.1.88"),
            throwOnUpdateLastHost = true,
        )
        val remoteRepository = FakeRemoteRepository(
            connectBehavior = { Result.success(expectedBaseUrl) },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.connect()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isBusy).isFalse()
        assertThat(state.isConnected).isTrue()
        assertThat(state.currentScreen).isEqualTo(AppScreen.Remote)
        assertThat(state.baseUrl).isEqualTo(expectedBaseUrl)
        assertThat(state.statusMessage).isEqualTo("连接成功")
        assertThat(state.isError).isFalse()
        assertThat(settingsRepository.updateLastHostCallCount).isEqualTo(1)
    }

    @Test
    fun `hardware volume down triggers next page when enabled`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(volumeKeysEnabled = true),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        val handled = viewModel.onHardwareButton(HardwareButton.VolumeDown)
        advanceUntilIdle()

        assertThat(handled).isTrue()
        assertThat(remoteRepository.lastAction).isEqualTo(RemoteAction.NextPage)
    }

    @Test
    fun `hardware volume down triggers next page with default preferences`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        val handled = viewModel.onHardwareButton(HardwareButton.VolumeDown)
        advanceUntilIdle()

        assertThat(handled).isTrue()
        assertThat(remoteRepository.lastAction).isEqualTo(RemoteAction.NextPage)
    }

    @Test
    fun `hardware button returns false when volume keys disabled`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(volumeKeysEnabled = false),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        val handled = viewModel.onHardwareButton(HardwareButton.VolumeDown)
        advanceUntilIdle()

        assertThat(handled).isFalse()
        assertThat(remoteRepository.sendCallCount).isEqualTo(0)
        assertThat(remoteRepository.lastAction).isNull()
    }

    @Test
    fun `hardware button returns false when disconnected`() = runTest {
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(volumeKeysEnabled = true),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()

        val handled = viewModel.onHardwareButton(HardwareButton.VolumeDown)
        advanceUntilIdle()

        assertThat(handled).isFalse()
        assertThat(remoteRepository.sendCallCount).isEqualTo(0)
        assertThat(remoteRepository.lastAction).isNull()
    }

    @Test
    fun `hardware button returns false while busy`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val sendGate = CompletableDeferred<Unit>()
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(volumeKeysEnabled = true),
        )
        val remoteRepository = FakeRemoteRepository(
            sendBehavior = { _, _ ->
                sendGate.await()
                Result.success(Unit)
            },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)
        viewModel.sendAction(RemoteAction.FullRefresh)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isBusy).isTrue()
        assertThat(remoteRepository.sendCallCount).isEqualTo(1)
        assertThat(remoteRepository.lastAction).isEqualTo(RemoteAction.FullRefresh)

        val handled = viewModel.onHardwareButton(HardwareButton.VolumeDown)

        assertThat(handled).isFalse()
        assertThat(remoteRepository.sendCallCount).isEqualTo(1)
        assertThat(remoteRepository.lastAction).isEqualTo(RemoteAction.FullRefresh)

        sendGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun `next page action does not lock ui while request is in flight`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val sendGate = CompletableDeferred<Unit>()
        val remoteRepository = FakeRemoteRepository(
            sendBehavior = { _, _ ->
                sendGate.await()
                Result.success(Unit)
            },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = FakeSettingsRepository(initialPreferences = UserPreferences()),
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
            pageTurnRateLimiter = PageTurnRateLimiter(
                minimumIntervalMillis = 100L,
                nowMillis = { 1_000L },
            ),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)
        viewModel.sendAction(RemoteAction.NextPage)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isBusy).isFalse()
        assertThat(remoteRepository.sendCallCount).isEqualTo(1)

        sendGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun `next page action can repeat after rate limit interval`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        var now = 1_000L
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = FakeSettingsRepository(initialPreferences = UserPreferences()),
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
            pageTurnRateLimiter = PageTurnRateLimiter(
                minimumIntervalMillis = 100L,
                nowMillis = { now },
            ),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        viewModel.sendAction(RemoteAction.NextPage)
        advanceUntilIdle()
        now = 1_100L
        viewModel.sendAction(RemoteAction.NextPage)
        advanceUntilIdle()

        assertThat(remoteRepository.sendCallCount).isEqualTo(2)
        assertThat(remoteRepository.lastAction).isEqualTo(RemoteAction.NextPage)
    }

    @Test
    fun `next page action is throttled inside rate limit interval`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        var now = 1_000L
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = FakeSettingsRepository(initialPreferences = UserPreferences()),
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
            pageTurnRateLimiter = PageTurnRateLimiter(
                minimumIntervalMillis = 100L,
                nowMillis = { now },
            ),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        viewModel.sendAction(RemoteAction.NextPage)
        advanceUntilIdle()
        now = 1_050L
        viewModel.sendAction(RemoteAction.NextPage)
        advanceUntilIdle()

        assertThat(remoteRepository.sendCallCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.statusMessage).isEqualTo("已发送：下一页")
    }

    @Test
    fun `toggle rotation success updates rotation mode and status`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        viewModel.toggleRotation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(remoteRepository.setRotationCallCount).isEqualTo(1)
        assertThat(remoteRepository.lastSetRotationBaseUrl).isEqualTo(baseUrl)
        assertThat(remoteRepository.lastRotationMode).isEqualTo(1)
        assertThat(state.rotationMode).isEqualTo(1)
        assertThat(state.statusMessage).isEqualTo("旋转模式已更新: 1")
        assertThat(state.isError).isFalse()
    }

    @Test
    fun `toggle rotation failure keeps busy false and shows error`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val rotationGate = CompletableDeferred<Unit>()
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository(
            setRotationBehavior = { _, _ ->
                rotationGate.await()
                Result.failure(IllegalStateException("rotate failed"))
            },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        viewModel.toggleRotation()
        assertThat(viewModel.uiState.value.isBusy).isTrue()
        rotationGate.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(remoteRepository.setRotationCallCount).isEqualTo(1)
        assertThat(state.isBusy).isFalse()
        assertThat(state.rotationMode).isEqualTo(0)
        assertThat(state.isError).isTrue()
        assertThat(state.statusMessage).isEqualTo("旋转失败")
    }

    @Test
    fun `take screenshot success updates status with saved location`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val screenshotBytes = byteArrayOf(1, 2, 3, 4)
        val savedLocation = "content://media/external/images/media/123"
        val screenshotGate = CompletableDeferred<Unit>()
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository(
            screenshotStreamProvider = {
                screenshotGate.await()
                ByteArrayInputStream(screenshotBytes)
            },
        )
        val screenshotSaver = FakeScreenshotSaver(location = savedLocation)
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = screenshotSaver,
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        viewModel.takeScreenshot()
        assertThat(viewModel.uiState.value.isBusy).isTrue()
        assertThat(viewModel.uiState.value.statusMessage).isEqualTo("截图中...")

        screenshotGate.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(remoteRepository.openScreenshotCallCount).isEqualTo(1)
        assertThat(remoteRepository.lastScreenshotBaseUrl).isEqualTo(baseUrl)
        assertThat(screenshotSaver.saveCallCount).isEqualTo(1)
        assertThat(screenshotSaver.lastSavedBytes.toList()).isEqualTo(screenshotBytes.toList())
        assertThat(state.statusMessage).isEqualTo("截图已保存: $savedLocation")
        assertThat(state.isError).isFalse()
        assertThat(state.isBusy).isFalse()
    }

    @Test
    fun `take screenshot failure clears busy and shows error`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val screenshotGate = CompletableDeferred<Unit>()
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository(
            screenshotStreamProvider = {
                screenshotGate.await()
                ByteArrayInputStream(byteArrayOf(1, 2, 3))
            },
        )
        val screenshotSaver = FakeScreenshotSaver(shouldThrow = true)
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = screenshotSaver,
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)

        viewModel.takeScreenshot()
        assertThat(viewModel.uiState.value.isBusy).isTrue()
        screenshotGate.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(remoteRepository.openScreenshotCallCount).isEqualTo(1)
        assertThat(screenshotSaver.saveCallCount).isEqualTo(1)
        assertThat(state.isBusy).isFalse()
        assertThat(state.isError).isTrue()
        assertThat(state.statusMessage).isEqualTo("截图失败")
    }

    @Test
    fun `seed connected state resets rotation mode`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)
        viewModel.toggleRotation()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.rotationMode).isEqualTo(1)

        viewModel.seedConnectedState(baseUrl)

        assertThat(viewModel.uiState.value.rotationMode).isEqualTo(0)
    }

    @Test
    fun `disconnect resets rotation mode`() = runTest {
        val baseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(),
        )
        val remoteRepository = FakeRemoteRepository()
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(baseUrl)
        viewModel.toggleRotation()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.rotationMode).isEqualTo(1)

        viewModel.disconnect()

        assertThat(viewModel.uiState.value.rotationMode).isEqualTo(0)
    }

    @Test
    fun `connect success resets rotation mode`() = runTest {
        val expectedBaseUrl = "http://192.168.1.88:8080"
        val settingsRepository = FakeSettingsRepository(
            initialPreferences = UserPreferences(lastHost = "192.168.1.88"),
        )
        val remoteRepository = FakeRemoteRepository(
            connectBehavior = { Result.success(expectedBaseUrl) },
        )
        val viewModel = MainViewModel(
            remoteRepository = remoteRepository,
            settingsRepository = settingsRepository,
            screenshotSaver = FakeScreenshotSaver(),
            volumeKeyActionResolver = VolumeKeyActionResolver(),
        )

        advanceUntilIdle()
        viewModel.seedConnectedState(expectedBaseUrl)
        viewModel.toggleRotation()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.rotationMode).isEqualTo(1)

        viewModel.connect()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.rotationMode).isEqualTo(0)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeRemoteRepository(
    private val connectBehavior: suspend (String) -> Result<String> = {
        Result.success("http://127.0.0.1:8080")
    },
    private val sendBehavior: suspend (String, RemoteAction) -> Result<Unit> = { _, _ ->
        Result.success(Unit)
    },
    private val setRotationBehavior: suspend (String, Int) -> Result<Unit> = { _, _ ->
        Result.success(Unit)
    },
    private val screenshotStreamProvider: suspend (String) -> InputStream = {
        ByteArrayInputStream(byteArrayOf())
    },
) : KoreaderRemoteRepository {
    var lastConnectInput: String? = null
    var connectCallCount: Int = 0
    var lastAction: RemoteAction? = null
    var sendCallCount: Int = 0
    var lastSetRotationBaseUrl: String? = null
    var lastRotationMode: Int? = null
    var setRotationCallCount: Int = 0
    var openScreenshotCallCount: Int = 0
    var lastScreenshotBaseUrl: String? = null

    override suspend fun connect(rawInput: String): Result<String> {
        connectCallCount += 1
        lastConnectInput = rawInput
        return connectBehavior(rawInput)
    }

    override suspend fun send(baseUrl: String, action: RemoteAction): Result<Unit> {
        sendCallCount += 1
        lastAction = action
        return sendBehavior(baseUrl, action)
    }

    override suspend fun setRotation(baseUrl: String, rotationMode: Int): Result<Unit> {
        setRotationCallCount += 1
        lastSetRotationBaseUrl = baseUrl
        lastRotationMode = rotationMode
        return setRotationBehavior(baseUrl, rotationMode)
    }

    override suspend fun openScreenshotStream(baseUrl: String): InputStream {
        openScreenshotCallCount += 1
        lastScreenshotBaseUrl = baseUrl
        return screenshotStreamProvider(baseUrl)
    }
}

private class FakeSettingsRepository(
    initialPreferences: UserPreferences,
    private val throwOnUpdateLastHost: Boolean = false,
) : SettingsRepository {
    private val mutablePreferences = MutableStateFlow(initialPreferences)
    var lastUpdatedHost: String? = null
    var updateLastHostCallCount: Int = 0

    override val preferencesFlow: Flow<UserPreferences> = mutablePreferences.asStateFlow()

    override suspend fun updateLastHost(value: String) {
        updateLastHostCallCount += 1
        if (throwOnUpdateLastHost) {
            throw IllegalStateException("persist failed")
        }
        lastUpdatedHost = value
        mutablePreferences.value = mutablePreferences.value.copy(lastHost = value)
    }

    override suspend fun updateDarkTheme(value: Boolean) {
        mutablePreferences.value = mutablePreferences.value.copy(darkTheme = value)
    }

    override suspend fun updateVolumeKeysEnabled(value: Boolean) {
        mutablePreferences.value = mutablePreferences.value.copy(volumeKeysEnabled = value)
    }

    override suspend fun updateInvertVolumeKeys(value: Boolean) {
        mutablePreferences.value = mutablePreferences.value.copy(invertVolumeKeys = value)
    }
}

private class FakeScreenshotSaver(
    private val location: String = "fake://screenshot.png",
    private val shouldThrow: Boolean = false,
) : ScreenshotSaver {
    var saveCallCount: Int = 0
    var lastSavedBytes: ByteArray = byteArrayOf()

    override suspend fun save(stream: InputStream): String {
        saveCallCount += 1
        lastSavedBytes = stream.readBytes()
        if (shouldThrow) {
            throw IllegalStateException("save failed")
        }
        return location
    }
}
