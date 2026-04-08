package io.github.hugo1120.koreaderremote.ui.state

import io.github.hugo1120.koreaderremote.domain.model.AppScreen
import io.github.hugo1120.koreaderremote.domain.model.ControlMode
import io.github.hugo1120.koreaderremote.domain.model.UserPreferences

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.Connect,
    val hostInput: String = "",
    val portInput: String = "8080",
    val baseUrl: String = "",
    val isConnected: Boolean = false,
    val isBusy: Boolean = false,
    val rotationMode: Int = 0,
    val statusMessage: String = "",
    val isError: Boolean = false,
    val currentControlMode: ControlMode = ControlMode.Button,
    val preferences: UserPreferences = UserPreferences(),
)
