package io.github.hugo1120.koreaderremote.domain.model

enum class ControlMode {
    Button,
    Blind,
}

data class UserPreferences(
    val lastHost: String = "",
    val lastPort: Int = 8080,
    val recentHosts: List<String> = emptyList(),
    val preferredSubnetPrefix: String = "",
    val darkTheme: Boolean = false,
    val volumeKeysEnabled: Boolean = true,
    val invertVolumeKeys: Boolean = false,
    val lastControlMode: ControlMode = ControlMode.Button,
)
