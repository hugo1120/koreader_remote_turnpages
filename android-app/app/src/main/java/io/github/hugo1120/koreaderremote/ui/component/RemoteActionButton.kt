package io.github.hugo1120.koreaderremote.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    subtitle: String? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasis: RemoteActionEmphasis = RemoteActionEmphasis.Secondary,
    minHeightDp: Int = if (emphasis == RemoteActionEmphasis.Primary) 132 else 98,
    iconSizeDp: Int = if (emphasis == RemoteActionEmphasis.Primary) 28 else 22,
    onClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.extraLarge
    val primary = emphasis == RemoteActionEmphasis.Primary
    val cardColors = if (primary) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    val iconContainerColor = if (primary) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
    }
    val horizontalPadding = if (primary) 18.dp else 14.dp
    val verticalPadding = if (primary) 14.dp else 10.dp
    val itemSpacing = if (primary) 14.dp else 10.dp
    val iconContainerPadding = if (primary) 12.dp else 8.dp
    val gradient = if (primary) {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            ),
        )
    }

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = cardColors,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = if (primary) 0.28f else 0.18f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (primary) 10.dp else 4.dp,
            pressedElevation = if (primary) 4.dp else 2.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .background(gradient)
                .height(minHeightDp.dp)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(iconContainerColor)
                    .padding(iconContainerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(iconSizeDp.dp),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = if (primary) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    textAlign = TextAlign.Start,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
