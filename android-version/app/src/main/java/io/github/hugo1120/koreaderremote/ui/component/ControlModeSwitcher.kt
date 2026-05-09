package io.github.hugo1120.koreaderremote.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hugo1120.koreaderremote.domain.model.ControlMode

@Composable
fun ControlModeSwitcher(
    selectedMode: ControlMode,
    onModeSelected: (ControlMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ControlModeItem(
            title = "按钮",
            selected = selectedMode == ControlMode.Button,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(ControlMode.Button) },
        )
        ControlModeItem(
            title = "盲操",
            selected = selectedMode == ControlMode.Blind,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(ControlMode.Blind) },
        )
    }
}

@Composable
private fun ControlModeItem(
    title: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = title)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(44.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(text = title)
        }
    }
}
