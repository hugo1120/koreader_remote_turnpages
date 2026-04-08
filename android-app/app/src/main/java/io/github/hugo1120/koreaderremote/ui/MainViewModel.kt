package io.github.hugo1120.koreaderremote.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hugo1120.koreaderremote.data.network.KoreaderHttpClient
import io.github.hugo1120.koreaderremote.data.repository.KoreaderRemoteRepository
import io.github.hugo1120.koreaderremote.data.settings.SettingsRepository
import io.github.hugo1120.koreaderremote.domain.model.AppScreen
import io.github.hugo1120.koreaderremote.domain.model.ControlMode
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import io.github.hugo1120.koreaderremote.platform.input.HardwareButton
import io.github.hugo1120.koreaderremote.platform.input.PageTurnRateLimiter
import io.github.hugo1120.koreaderremote.platform.input.VolumeKeyActionResolver
import io.github.hugo1120.koreaderremote.platform.storage.ScreenshotSaver
import io.github.hugo1120.koreaderremote.ui.state.MainUiState
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val remoteRepository: KoreaderRemoteRepository,
    private val settingsRepository: SettingsRepository,
    private val screenshotSaver: ScreenshotSaver,
    private val volumeKeyActionResolver: VolumeKeyActionResolver,
    private val pageTurnRateLimiter: PageTurnRateLimiter = PageTurnRateLimiter(),
) : ViewModel() {
    private val numericTailRegex = Regex("^\\d+$")
    private val schemeRegex = Regex("^[a-zA-Z][a-zA-Z\\d+\\-.]*://")
    private val bracketedHostWithPortRegex = Regex("^\\[[^\\]]+\\]:\\d+$")
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    private var latestPageTurnRequestToken: Long = 0L
    private var didRestoreConnectionInputs = false
    private var pendingControlMode: ControlMode? = null

    init {
        viewModelScope.launch {
            settingsRepository.preferencesFlow.collect { preferences ->
                _uiState.update { state ->
                    val shouldRestoreConnectionInputs = !didRestoreConnectionInputs
                    if (shouldRestoreConnectionInputs) {
                        didRestoreConnectionInputs = true
                    }
                    if (pendingControlMode == preferences.lastControlMode) {
                        pendingControlMode = null
                    }
                    state.copy(
                        preferences = preferences,
                        currentControlMode = pendingControlMode ?: preferences.lastControlMode,
                        hostInput = if (shouldRestoreConnectionInputs) {
                            restoreHostInput(preferences.lastHost)
                        } else {
                            state.hostInput
                        },
                        portInput = if (shouldRestoreConnectionInputs) {
                            restorePortInput(preferences.lastPort)
                        } else {
                            state.portInput
                        },
                    )
                }
            }
        }
    }

    fun onHostChanged(value: String) {
        didRestoreConnectionInputs = true
        _uiState.update { state ->
            state.copy(hostInput = value)
        }
    }

    fun onPortChanged(value: String) {
        didRestoreConnectionInputs = true
        val sanitizedPort = value.filter { it.isDigit() }.take(MAX_PORT_INPUT_LENGTH)
        _uiState.update { state ->
            state.copy(portInput = sanitizedPort)
        }
    }

    fun onRecentHostSelected(value: String) {
        val trimmedValue = value.trim()
        if (trimmedValue.isBlank()) {
            return
        }

        didRestoreConnectionInputs = true
        val parsed = runCatching {
            KoreaderHttpClient.parseHostPort(trimmedValue)
        }.getOrNull()

        _uiState.update { state ->
            if (parsed == null) {
                state.copy(
                    hostInput = trimmedValue,
                    portInput = restorePortInput(state.preferences.lastPort),
                )
            } else {
                state.copy(
                    hostInput = parsed.host,
                    portInput = parsed.port.toString(),
                )
            }
        }
    }

    fun connect() {
        val currentState = uiState.value
        if (currentState.isBusy) {
            return
        }

        val input = resolveConnectInput(
            rawHostInput = currentState.hostInput,
            rawPortInput = currentState.portInput,
            preferredSubnetPrefix = currentState.preferences.preferredSubnetPrefix,
        )
        if (input.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isBusy = false,
                    isConnected = false,
                    currentScreen = AppScreen.Connect,
                    baseUrl = "",
                    isError = true,
                    statusMessage = "连接失败",
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                isBusy = true,
                isError = false,
                statusMessage = "连接中...",
            )
        }

        viewModelScope.launch {
            val connectResult = runCatching {
                remoteRepository.connect(input)
            }.getOrElse { throwable ->
                Result.failure(throwable)
            }

            connectResult
                .onSuccess { baseUrl ->
                    val parsedConnection = KoreaderHttpClient.parseHostPort(baseUrl)
                    _uiState.update { state ->
                        state.copy(
                            isBusy = false,
                            isConnected = true,
                            currentScreen = AppScreen.Remote,
                            baseUrl = baseUrl,
                            hostInput = parsedConnection.host,
                            portInput = parsedConnection.port.toString(),
                            rotationMode = 0,
                            statusMessage = "连接成功",
                            isError = false,
                        )
                    }
                    runCatching {
                        settingsRepository.rememberSuccessfulConnection(baseUrl)
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isBusy = false,
                            isConnected = false,
                            currentScreen = AppScreen.Connect,
                            baseUrl = "",
                            isError = true,
                            statusMessage = connectFailureMessage(it),
                        )
                    }
                }
        }
    }

    fun seedConnectedState(baseUrl: String) {
        _uiState.update { state ->
            state.copy(
                currentScreen = AppScreen.Remote,
                baseUrl = baseUrl,
                isConnected = true,
                isBusy = false,
                rotationMode = 0,
                isError = false,
                statusMessage = "连接成功",
            )
        }
    }

    fun sendAction(action: RemoteAction) {
        if (action == RemoteAction.PreviousPage || action == RemoteAction.NextPage) {
            sendPageTurnAction(action)
            return
        }

        val currentState = uiState.value
        if (currentState.isBusy) {
            return
        }

        if (!currentState.isConnected || currentState.baseUrl.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isError = true,
                    statusMessage = "未连接设备",
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                isBusy = true,
                isError = false,
                statusMessage = "发送指令中...",
            )
        }

        viewModelScope.launch {
            remoteRepository.send(currentState.baseUrl, action)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isBusy = false,
                            isError = false,
                            statusMessage = actionSuccessMessage(action),
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isBusy = false,
                            isError = true,
                            statusMessage = actionFailureMessage(action),
                        )
                    }
                }
        }
    }

    private fun sendPageTurnAction(action: RemoteAction) {
        val currentState = uiState.value
        if (currentState.isBusy) {
            return
        }

        if (!currentState.isConnected || currentState.baseUrl.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isError = true,
                    statusMessage = "未连接设备",
                )
            }
            return
        }

        if (!pageTurnRateLimiter.tryAcquire()) {
            return
        }

        latestPageTurnRequestToken += 1
        val requestToken = latestPageTurnRequestToken
        viewModelScope.launch {
            remoteRepository.send(currentState.baseUrl, action)
                .onSuccess {
                    if (requestToken == latestPageTurnRequestToken) {
                        _uiState.update { state ->
                            state.copy(
                                isError = false,
                                statusMessage = actionSuccessMessage(action),
                            )
                        }
                    }
                }
                .onFailure {
                    if (requestToken == latestPageTurnRequestToken) {
                        _uiState.update { state ->
                            state.copy(
                                isError = true,
                                statusMessage = actionFailureMessage(action),
                            )
                        }
                    }
                }
        }
    }

    fun onHardwareButton(button: HardwareButton): Boolean {
        val state = uiState.value
        if (state.isBusy) {
            return false
        }

        val mappedAction = volumeKeyActionResolver.resolve(
            button = button,
            preferences = state.preferences,
            isConnected = state.isConnected,
        ) ?: return false

        sendAction(mappedAction)
        return true
    }

    fun openSettings() {
        if (uiState.value.isBusy) {
            return
        }

        _uiState.update { state ->
            state.copy(currentScreen = AppScreen.Settings)
        }
    }

    fun closeSettings() {
        _uiState.update { state ->
            state.copy(
                currentScreen = if (state.isConnected) {
                    AppScreen.Remote
                } else {
                    AppScreen.Connect
                },
            )
        }
    }

    fun setVolumeKeysEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                settingsRepository.updateVolumeKeysEnabled(enabled)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isError = true,
                        statusMessage = "更新音量键设置失败",
                    )
                }
            }
        }
    }

    fun setInvertVolumeKeys(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                settingsRepository.updateInvertVolumeKeys(enabled)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isError = true,
                        statusMessage = "更新音量键方向失败",
                    )
                }
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                settingsRepository.updateDarkTheme(enabled)
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isError = true,
                        statusMessage = "更新主题失败",
                    )
                }
            }
        }
    }

    fun setControlMode(mode: ControlMode) {
        if (uiState.value.currentControlMode == mode) {
            return
        }

        pendingControlMode = mode
        _uiState.update { state ->
            state.copy(
                currentControlMode = mode,
                isError = false,
            )
        }

        viewModelScope.launch {
            runCatching {
                settingsRepository.updateLastControlMode(mode)
            }.onFailure {
                pendingControlMode = null
                _uiState.update { state ->
                    state.copy(
                        currentControlMode = state.preferences.lastControlMode,
                        isError = true,
                        statusMessage = "更新控制模式失败",
                    )
                }
            }
        }
    }

    fun toggleControlMode() {
        val nextMode = when (uiState.value.currentControlMode) {
            ControlMode.Button -> ControlMode.Blind
            ControlMode.Blind -> ControlMode.Button
        }
        setControlMode(nextMode)
    }

    fun disconnect() {
        if (uiState.value.isBusy) {
            return
        }

        _uiState.update { state ->
            state.copy(
                currentScreen = AppScreen.Connect,
                baseUrl = "",
                isConnected = false,
                isBusy = false,
                rotationMode = 0,
                isError = false,
                statusMessage = "已断开连接",
            )
        }
    }

    fun toggleRotation() {
        val currentState = uiState.value
        if (currentState.isBusy) {
            return
        }

        if (!currentState.isConnected || currentState.baseUrl.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isError = true,
                    statusMessage = "未连接设备",
                )
            }
            return
        }

        val nextMode = if (currentState.rotationMode == 0) 1 else 0
        _uiState.update { state ->
            state.copy(
                isBusy = true,
                isError = false,
                statusMessage = "旋转中...",
            )
        }

        viewModelScope.launch {
            remoteRepository.setRotation(currentState.baseUrl, nextMode)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isBusy = false,
                            rotationMode = nextMode,
                            isError = false,
                            statusMessage = "旋转模式已更新: $nextMode",
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isBusy = false,
                            isError = true,
                            statusMessage = "旋转失败",
                        )
                    }
                }
        }
    }

    fun takeScreenshot() {
        val currentState = uiState.value
        if (currentState.isBusy) {
            return
        }

        if (!currentState.isConnected || currentState.baseUrl.isBlank()) {
            _uiState.update { state ->
                state.copy(
                    isError = true,
                    statusMessage = "未连接设备",
                )
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                isBusy = true,
                isError = false,
                statusMessage = "截图中...",
            )
        }

        viewModelScope.launch {
            runCatching {
                remoteRepository.openScreenshotStream(currentState.baseUrl).use { stream ->
                    screenshotSaver.save(stream)
                }
            }.onSuccess { location ->
                _uiState.update { state ->
                    state.copy(
                        isBusy = false,
                        isError = false,
                        statusMessage = "截图已保存: $location",
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isBusy = false,
                        isError = true,
                        statusMessage = "截图失败",
                    )
                }
            }
        }
    }

    private fun actionSuccessMessage(action: RemoteAction): String =
        when (action) {
            RemoteAction.PreviousPage -> "已发送：上一页"
            RemoteAction.NextPage -> "已发送：下一页"
            RemoteAction.FullRefresh -> "已发送：全刷"
            RemoteAction.Suspend -> "已发送：休眠"
        }

    private fun actionFailureMessage(action: RemoteAction): String =
        when (action) {
            RemoteAction.PreviousPage -> "发送失败：上一页"
            RemoteAction.NextPage -> "发送失败：下一页"
            RemoteAction.FullRefresh -> "发送失败：全刷"
            RemoteAction.Suspend -> "发送失败：休眠"
        }

    private fun connectFailureMessage(throwable: Throwable): String {
        val rootCause = rootCauseOf(throwable)
        val detail = when (rootCause) {
            is SocketTimeoutException -> rootCause.message ?: "连接超时"
            is ConnectException -> "连接被拒绝"
            is UnknownHostException -> "地址解析失败"
            is IllegalArgumentException -> "地址格式无效"
            else -> {
                val message = rootCause.message?.trim().orEmpty()
                when {
                    message.isBlank() -> rootCause.javaClass.simpleName
                    message.equals("connect failed", ignoreCase = true) -> "无法访问 KOReader 接口"
                    else -> message
                }
            }
        }

        return "连接失败：$detail"
    }

    private fun rootCauseOf(throwable: Throwable): Throwable {
        var current = throwable
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current
    }

    private fun resolveConnectInput(
        rawHostInput: String,
        rawPortInput: String,
        preferredSubnetPrefix: String,
    ): String {
        val trimmedHostInput = rawHostInput.trim()
        if (trimmedHostInput.isBlank()) {
            return ""
        }

        val resolvedHost = if (trimmedHostInput.matches(numericTailRegex) && preferredSubnetPrefix.isNotBlank()) {
            "$preferredSubnetPrefix.$trimmedHostInput"
        } else {
            trimmedHostInput
        }

        if (schemeRegex.containsMatchIn(resolvedHost) || hasExplicitPort(resolvedHost)) {
            return resolvedHost
        }

        val resolvedPort = rawPortInput.trim().toIntOrNull()
            ?.takeIf { it in 1..MAX_VALID_PORT }
            ?: DEFAULT_KOREADER_PORT
        val formattedHost = formatHostIfNeeded(resolvedHost)
        return if (resolvedPort == DEFAULT_KOREADER_PORT) {
            formattedHost
        } else {
            "$formattedHost:$resolvedPort"
        }
    }

    private fun hasExplicitPort(hostInput: String): Boolean {
        if (hostInput.startsWith("[")) {
            return bracketedHostWithPortRegex.matches(hostInput)
        }

        val colonIndex = hostInput.lastIndexOf(':')
        if (colonIndex <= 0 || colonIndex == hostInput.lastIndex) {
            return false
        }

        if (hostInput.count { it == ':' } > 1) {
            return false
        }

        val portPart = hostInput.substring(colonIndex + 1)
        return portPart.isNotBlank() && portPart.all { it.isDigit() }
    }

    private fun formatHostIfNeeded(host: String): String {
        return if (host.contains(":") && !host.startsWith("[")) {
            "[$host]"
        } else {
            host
        }
    }

    private fun restoreHostInput(host: String): String = host

    private fun restorePortInput(port: Int): String {
        return if (port in 1..MAX_VALID_PORT) {
            port.toString()
        } else {
            DEFAULT_KOREADER_PORT.toString()
        }
    }

    private companion object {
        const val DEFAULT_KOREADER_PORT = 8080
        const val MAX_PORT_INPUT_LENGTH = 5
        const val MAX_VALID_PORT = 65535
    }
}
