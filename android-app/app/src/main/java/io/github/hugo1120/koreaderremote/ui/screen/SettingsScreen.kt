package io.github.hugo1120.koreaderremote.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hugo1120.koreaderremote.ui.state.MainUiState

@Composable
fun SettingsScreen(
    state: MainUiState,
    onVolumeKeysEnabledChanged: (Boolean) -> Unit,
    onInvertVolumeKeysChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineSmall,
        )
        SwitchRow(
            title = "启用音量键翻页",
            checked = state.preferences.volumeKeysEnabled,
            onCheckedChange = onVolumeKeysEnabledChanged,
        )
        SwitchRow(
            title = "反转音量键方向",
            checked = state.preferences.invertVolumeKeys,
            onCheckedChange = onInvertVolumeKeysChanged,
        )
        SwitchRow(
            title = "深色主题",
            checked = state.preferences.darkTheme,
            onCheckedChange = onDarkThemeChanged,
        )
        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "返回")
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
