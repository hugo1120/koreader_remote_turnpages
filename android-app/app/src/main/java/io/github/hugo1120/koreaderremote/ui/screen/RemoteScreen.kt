package io.github.hugo1120.koreaderremote.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ScreenRotationAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import io.github.hugo1120.koreaderremote.ui.component.CenteredScreenLayout
import io.github.hugo1120.koreaderremote.ui.component.RemoteActionButton
import io.github.hugo1120.koreaderremote.ui.component.RemoteActionEmphasis
import io.github.hugo1120.koreaderremote.ui.state.MainUiState

@Composable
fun RemoteScreen(
    state: MainUiState,
    onAction: (RemoteAction) -> Unit,
    onRotate: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenSettings: () -> Unit,
    onDisconnect: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    CenteredScreenLayout(verticalSpacing = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "KOReader Remote",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = state.baseUrl,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = state.statusMessage.ifBlank { "已连接，准备翻页" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusPill(
                            icon = Icons.Rounded.Wifi,
                            text = "已连接",
                        )
                        StatusPill(
                            icon = Icons.Rounded.VolumeUp,
                            text = if (state.preferences.volumeKeysEnabled) {
                                "音量键已启用"
                            } else {
                                "音量键已关闭"
                            },
                        )
                    }
                }
            }
            ThemeToggleButton(
                isDarkTheme = state.preferences.darkTheme,
                onClick = onToggleTheme,
            )
        }

        RemoteActionButton(
            title = "上一页",
            subtitle = "向前回退一页，适合精细翻阅",
            icon = Icons.Rounded.ArrowBack,
            emphasis = RemoteActionEmphasis.Primary,
            enabled = !state.isBusy,
            onClick = { onAction(RemoteAction.PreviousPage) },
        )
        RemoteActionButton(
            title = "下一页",
            subtitle = "继续阅读下一页，支持高频连续点按",
            icon = Icons.Rounded.ArrowForward,
            emphasis = RemoteActionEmphasis.Primary,
            enabled = !state.isBusy,
            onClick = { onAction(RemoteAction.NextPage) },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RemoteActionButton(
                title = "旋转",
                subtitle = "切换屏幕方向",
                icon = Icons.Rounded.ScreenRotationAlt,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
                onClick = onRotate,
            )
            RemoteActionButton(
                title = "全刷",
                subtitle = "刷新墨水残影",
                icon = Icons.Rounded.Refresh,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
                onClick = { onAction(RemoteAction.FullRefresh) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RemoteActionButton(
                title = "截图",
                subtitle = "保存当前页面",
                icon = Icons.Rounded.PhotoCamera,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
                onClick = onScreenshot,
            )
            RemoteActionButton(
                title = "休眠",
                subtitle = "让设备进入休眠",
                icon = Icons.Rounded.Bedtime,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
                onClick = { onAction(RemoteAction.Suspend) },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RemoteActionButton(
                title = "设置",
                subtitle = "调整翻页偏好",
                icon = Icons.Rounded.Settings,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
                onClick = onOpenSettings,
            )
            RemoteActionButton(
                title = "断开",
                subtitle = "返回连接页面",
                icon = Icons.Rounded.LinkOff,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
                onClick = onDisconnect,
            )
        }
    }
}

@Composable
private fun StatusPill(
    icon: ImageVector,
    text: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FilledIconButton(onClick = onClick) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = if (isDarkTheme) "切换到浅色" else "切换到深色",
                )
            }
            Text(
                text = if (isDarkTheme) "日间" else "夜间",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
