package io.github.hugo1120.koreaderremote.domain.model

data class UserPreferences(
    val lastHost: String = "",
    val darkTheme: Boolean = false,
    val volumeKeysEnabled: Boolean = true,
    val invertVolumeKeys: Boolean = false,
)
