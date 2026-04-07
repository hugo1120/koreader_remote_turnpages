package io.github.hugo1120.koreaderremote.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import io.github.hugo1120.koreaderremote.ui.component.RemoteActionButton
import io.github.hugo1120.koreaderremote.ui.state.MainUiState

@Composable
fun RemoteScreen(
    state: MainUiState,
    onAction: (RemoteAction) -> Unit,
    onRotate: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenSettings: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "已连接：${state.baseUrl}",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        RemoteActionButton(
            text = "上一页",
            enabled = !state.isBusy,
            onClick = { onAction(RemoteAction.PreviousPage) },
        )
        RemoteActionButton(
            text = "下一页",
            enabled = !state.isBusy,
            onClick = { onAction(RemoteAction.NextPage) },
        )
        RemoteActionButton(
            text = "旋转",
            enabled = !state.isBusy,
            onClick = onRotate,
        )
        RemoteActionButton(
            text = "全刷",
            enabled = !state.isBusy,
            onClick = { onAction(RemoteAction.FullRefresh) },
        )
        RemoteActionButton(
            text = "截图",
            enabled = !state.isBusy,
            onClick = onScreenshot,
        )
        RemoteActionButton(
            text = "休眠",
            enabled = !state.isBusy,
            onClick = { onAction(RemoteAction.Suspend) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onOpenSettings,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "设置")
            }
            OutlinedButton(
                onClick = onDisconnect,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "断开")
            }
        }
        Text(
            text = state.statusMessage,
            color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
