package io.github.hugo1120.koreaderremote.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class RemoteActionEmphasis {
    Primary,
    Secondary,
}

@Composable
fun RemoteActionButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasis: RemoteActionEmphasis = RemoteActionEmphasis.Secondary,
    minHeightDp: Int = if (emphasis == RemoteActionEmphasis.Primary) 132 else 98,
    iconSizeDp: Int = if (emphasis == RemoteActionEmphasis.Primary) 28 else 22,
    onClick: () -> Unit,
) {
    val primary = emphasis == RemoteActionEmphasis.Primary
    val containerColor = if (primary) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (primary) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.55f),
            disabledContentColor = contentColor.copy(alpha = 0.65f),
        ),
        border = BorderStroke(
            width = 1.dp,
            color = contentColor.copy(alpha = if (primary) 0.14f else 0.10f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (primary) 10.dp else 6.dp,
            pressedElevation = if (primary) 4.dp else 3.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .height(minHeightDp.dp)
                .padding(
                    horizontal = if (primary) 16.dp else 12.dp,
                    vertical = if (primary) 14.dp else 10.dp,
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(iconSizeDp.dp),
            )
            Text(
                text = title,
                style = if (primary) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.titleMedium
                },
                textAlign = TextAlign.Center,
            )
        }
    }
}
