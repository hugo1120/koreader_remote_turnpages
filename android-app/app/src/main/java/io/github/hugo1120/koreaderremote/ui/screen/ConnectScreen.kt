package io.github.hugo1120.koreaderremote.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import io.github.hugo1120.koreaderremote.ui.component.CenteredScreenLayout
import io.github.hugo1120.koreaderremote.ui.state.MainUiState

@Composable
fun ConnectScreen(
    state: MainUiState,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onRecentHostClick: (String) -> Unit,
    onConnectClick: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val actionButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    CenteredScreenLayout(verticalSpacing = 18.dp) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "连接 KOReader",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "输入设备地址后即可进入遥控器首页，默认端口为 8080。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = state.hostInput,
                    onValueChange = onHostChanged,
                    singleLine = true,
                    label = { Text("地址") },
                    supportingText = { Text("支持完整 URL、host:port 或数字尾段（如 77）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.portInput,
                    onValueChange = onPortChanged,
                    singleLine = true,
                    label = { Text("端口") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.preferences.preferredSubnetPrefix.isNotBlank()) {
                    Text(
                        text = "常用网段：${state.preferences.preferredSubnetPrefix}.*",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.preferences.recentHosts.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "最近记录",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        state.preferences.recentHosts.take(5).forEach { hostPort ->
                            Button(
                                onClick = { onRecentHostClick(hostPort) },
                                enabled = !state.isBusy,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = hostPort,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = onConnectClick,
                    enabled = !state.isBusy,
                    colors = actionButtonColors,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = if (state.isBusy) "连接中..." else "进入遥控器")
                }
                Button(
                    onClick = onOpenSettings,
                    enabled = !state.isBusy,
                    colors = actionButtonColors,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "打开设置")
                }
                Text(
                    text = "默认音量键翻页已开启，连接后可直接用音量键控制。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = state.statusMessage,
            color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
