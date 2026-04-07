package io.github.hugo1120.koreaderremote.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onConnectClick: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    CenteredScreenLayout(verticalSpacing = 16.dp) {
        Text(
            text = "连接 KOReader",
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = state.hostInput,
            onValueChange = onHostChanged,
            singleLine = true,
            label = { Text("IP、主机名或 URL") },
            supportingText = { Text("默认端口 8080，也可输入 192.168.1.88:8081") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onConnectClick,
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = if (state.isBusy) "连接中..." else "连接")
        }
        TextButton(
            onClick = onOpenSettings,
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "设置")
        }
        Text(
            text = state.statusMessage,
            color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
