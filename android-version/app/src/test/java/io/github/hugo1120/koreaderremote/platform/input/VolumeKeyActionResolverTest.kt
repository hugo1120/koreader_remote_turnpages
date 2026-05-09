package io.github.hugo1120.koreaderremote.platform.input

import com.google.common.truth.Truth.assertThat
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import io.github.hugo1120.koreaderremote.domain.model.UserPreferences
import org.junit.Test

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

    @Test
    fun `returns null when not connected`() {
        val action = resolver.resolve(
            button = HardwareButton.VolumeUp,
            preferences = UserPreferences(volumeKeysEnabled = true),
            isConnected = false,
        )

        assertThat(action).isNull()
    }

    @Test
    fun `returns next page for volume up when inverted`() {
        val action = resolver.resolve(
            button = HardwareButton.VolumeUp,
            preferences = UserPreferences(volumeKeysEnabled = true, invertVolumeKeys = true),
            isConnected = true,
        )

        assertThat(action).isEqualTo(RemoteAction.NextPage)
    }

    @Test
    fun `returns next page for volume down when enabled`() {
        val action = resolver.resolve(
            button = HardwareButton.VolumeDown,
            preferences = UserPreferences(volumeKeysEnabled = true, invertVolumeKeys = false),
            isConnected = true,
        )

        assertThat(action).isEqualTo(RemoteAction.NextPage)
    }

    @Test
    fun `returns previous page for volume down when inverted`() {
        val action = resolver.resolve(
            button = HardwareButton.VolumeDown,
            preferences = UserPreferences(volumeKeysEnabled = true, invertVolumeKeys = true),
            isConnected = true,
        )

        assertThat(action).isEqualTo(RemoteAction.PreviousPage)
    }
}
