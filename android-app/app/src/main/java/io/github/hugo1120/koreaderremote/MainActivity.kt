package io.github.hugo1120.koreaderremote

import android.view.KeyEvent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hugo1120.koreaderremote.app.KOReaderApp
import io.github.hugo1120.koreaderremote.domain.model.AppScreen
import io.github.hugo1120.koreaderremote.platform.input.HardwareButton
import io.github.hugo1120.koreaderremote.ui.MainViewModel
import io.github.hugo1120.koreaderremote.ui.MainViewModelFactory
import io.github.hugo1120.koreaderremote.ui.screen.ConnectScreen
import io.github.hugo1120.koreaderremote.ui.screen.RemoteScreen
import io.github.hugo1120.koreaderremote.ui.screen.SettingsScreen
import io.github.hugo1120.koreaderremote.ui.theme.KOReaderRemoteTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as KOReaderApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle().value
            KOReaderRemoteTheme(darkTheme = state.preferences.darkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val handled = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> viewModel.onHardwareButton(HardwareButton.VolumeUp)
            KeyEvent.KEYCODE_VOLUME_DOWN -> viewModel.onHardwareButton(HardwareButton.VolumeDown)
            else -> false
        }
        return if (handled) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}
