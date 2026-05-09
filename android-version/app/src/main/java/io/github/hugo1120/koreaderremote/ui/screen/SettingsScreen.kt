package io.github.hugo1120.koreaderremote.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hugo1120.koreaderremote.ui.component.CenteredScreenLayout
import io.github.hugo1120.koreaderremote.ui.state.MainUiState

@Composable
fun SettingsScreen(
    state: MainUiState,
    onVolumeKeysEnabledChanged: (Boolean) -> Unit,
    onInvertVolumeKeysChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit,
) {
    CenteredScreenLayout(verticalSpacing = 16.dp) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "调节翻页习惯和显示风格。首页右上角也可以直接切换日间/夜间。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SwitchCard(
            title = "启用音量键翻页",
            subtitle = "连接后直接用手机音量键控制上一页和下一页。",
            checked = state.preferences.volumeKeysEnabled,
            onCheckedChange = onVolumeKeysEnabledChanged,
        )
        SwitchCard(
            title = "反转音量键方向",
            subtitle = "切换音量加减和上一页/下一页的映射方向。",
            checked = state.preferences.invertVolumeKeys,
            onCheckedChange = onInvertVolumeKeysChanged,
        )
        SwitchCard(
            title = "深色主题",
            subtitle = "保留这里的开关，首页太阳/月亮按钮也会同步切换。",
            checked = state.preferences.darkTheme,
            onCheckedChange = onDarkThemeChanged,
        )
        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "返回遥控器")
        }
    }
}

@Composable
private fun SwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
