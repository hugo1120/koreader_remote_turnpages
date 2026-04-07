package io.github.hugo1120.koreaderremote.platform.input

import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import io.github.hugo1120.koreaderremote.domain.model.UserPreferences

class VolumeKeyActionResolver {
    fun resolve(
        button: HardwareButton,
        preferences: UserPreferences,
        isConnected: Boolean,
    ): RemoteAction? {
        if (!preferences.volumeKeysEnabled || !isConnected) {
            return null
        }

        return if (preferences.invertVolumeKeys) {
            when (button) {
                HardwareButton.VolumeUp -> RemoteAction.NextPage
                HardwareButton.VolumeDown -> RemoteAction.PreviousPage
            }
        } else {
            when (button) {
                HardwareButton.VolumeUp -> RemoteAction.PreviousPage
                HardwareButton.VolumeDown -> RemoteAction.NextPage
            }
        }
    }
}
