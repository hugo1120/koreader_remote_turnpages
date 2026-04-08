package io.github.hugo1120.koreaderremote.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ScreenRotationAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val layout = remoteLayoutSpecForHeight(maxHeight.value.toInt())
        val contentAlignment = if (layout.heightBand == RemoteHeightBand.Tall) {
            Alignment.Center
        } else {
            Alignment.TopCenter
        }

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = layout.horizontalPaddingDp.dp,
                    vertical = layout.verticalPaddingDp.dp,
                ),
            contentAlignment = contentAlignment,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = layout.maxContentWidthDp.dp),
                verticalArrangement = Arrangement.spacedBy(layout.blockSpacingDp.dp),
            ) {
                HeaderCard(
                    state = state,
                    layout = layout,
                    onToggleTheme = onToggleTheme,
                )

                RemoteActionButton(
                    title = "上一页",
                    icon = Icons.Rounded.ArrowBack,
                    emphasis = RemoteActionEmphasis.Primary,
                    minHeightDp = layout.mainButtonHeightDp,
                    iconSizeDp = layout.mainButtonIconSizeDp,
                    enabled = !state.isBusy,
                    onClick = { onAction(RemoteAction.PreviousPage) },
                )
                RemoteActionButton(
                    title = "下一页",
                    icon = Icons.Rounded.ArrowForward,
                    emphasis = RemoteActionEmphasis.Primary,
                    minHeightDp = layout.mainButtonHeightDp,
                    iconSizeDp = layout.mainButtonIconSizeDp,
                    enabled = !state.isBusy,
                    onClick = { onAction(RemoteAction.NextPage) },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layout.blockSpacingDp.dp),
                ) {
                    RemoteActionButton(
                        title = "旋转",
                        icon = Icons.Rounded.ScreenRotationAlt,
                        modifier = Modifier.weight(1f),
                        minHeightDp = layout.toolButtonHeightDp,
                        iconSizeDp = layout.toolIconSizeDp,
                        enabled = !state.isBusy,
                        onClick = onRotate,
                    )
                    RemoteActionButton(
                        title = "全刷",
                        icon = Icons.Rounded.Refresh,
                        modifier = Modifier.weight(1f),
                        minHeightDp = layout.toolButtonHeightDp,
                        iconSizeDp = layout.toolIconSizeDp,
                        enabled = !state.isBusy,
                        onClick = { onAction(RemoteAction.FullRefresh) },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layout.blockSpacingDp.dp),
                ) {
                    RemoteActionButton(
                        title = "截图",
                        icon = Icons.Rounded.PhotoCamera,
                        modifier = Modifier.weight(1f),
                        minHeightDp = layout.toolButtonHeightDp,
                        iconSizeDp = layout.toolIconSizeDp,
                        enabled = !state.isBusy,
                        onClick = onScreenshot,
                    )
                    RemoteActionButton(
                        title = "休眠",
                        icon = Icons.Rounded.Bedtime,
                        modifier = Modifier.weight(1f),
                        minHeightDp = layout.toolButtonHeightDp,
                        iconSizeDp = layout.toolIconSizeDp,
                        enabled = !state.isBusy,
                        onClick = { onAction(RemoteAction.Suspend) },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(layout.blockSpacingDp.dp),
                ) {
                    OutlinedButton(
                        onClick = onOpenSettings,
                        enabled = !state.isBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(layout.bottomButtonHeightDp.dp),
                    ) {
                        Text(text = "设置")
                    }
                    OutlinedButton(
                        onClick = onDisconnect,
                        enabled = !state.isBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(layout.bottomButtonHeightDp.dp),
                    ) {
                        Text(text = "断开")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    state: MainUiState,
    layout: RemoteLayoutSpec,
    onToggleTheme: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = layout.headerHeightDp.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = layout.headerPaddingHorizontalDp.dp,
                vertical = layout.headerPaddingVerticalDp.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(
                if (layout.heightBand == RemoteHeightBand.Compact) 6.dp else 8.dp,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "KOReader Remote",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                FilledIconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(layout.bottomButtonHeightDp.dp),
                ) {
                    Icon(
                        imageVector = if (state.preferences.darkTheme) {
                            Icons.Rounded.LightMode
                        } else {
                            Icons.Rounded.DarkMode
                        },
                        contentDescription = if (state.preferences.darkTheme) "切换到浅色" else "切换到深色",
                    )
                }
            }
            Text(
                text = state.baseUrl.removePrefix("http://").removePrefix("https://"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.statusMessage.ifBlank { "连接稳定" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = layout.headerStatusMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    icon = if (state.isConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                    text = if (state.isConnected) "已连接" else "未连接",
                    layout = layout,
                )
                StatusPill(
                    icon = Icons.Rounded.VolumeUp,
                    text = if (state.preferences.volumeKeysEnabled) "音量键开启" else "音量键关闭",
                    layout = layout,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    icon: ImageVector,
    text: String,
    layout: RemoteLayoutSpec,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = layout.chipHorizontalPaddingDp.dp,
                vertical = layout.chipVerticalPaddingDp.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}
