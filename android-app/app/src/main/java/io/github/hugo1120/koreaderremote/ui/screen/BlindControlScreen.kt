package io.github.hugo1120.koreaderremote.ui.screen

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.hugo1120.koreaderremote.domain.model.ControlMode
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import io.github.hugo1120.koreaderremote.platform.input.SwipeGestureInput
import io.github.hugo1120.koreaderremote.platform.input.SwipePageTurnDetector
import io.github.hugo1120.koreaderremote.ui.component.ControlModeSwitcher
import io.github.hugo1120.koreaderremote.ui.state.MainUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BlindControlScreen(
    state: MainUiState,
    onAction: (RemoteAction) -> Unit,
    onModeSelected: (ControlMode) -> Unit,
    onToggleTheme: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val detector = remember { SwipePageTurnDetector() }
    var gestureAreaSize by remember { mutableStateOf(IntSize.Zero) }
    var startX by remember { mutableStateOf<Float?>(null) }
    var startY by remember { mutableStateOf<Float?>(null) }
    var endX by remember { mutableStateOf<Float?>(null) }
    var endY by remember { mutableStateOf<Float?>(null) }
    var pointerCount by remember { mutableStateOf(0) }
    var feedbackLabel by remember { mutableStateOf<String?>(null) }
    var feedbackSequence by remember { mutableStateOf(0) }

    LaunchedEffect(feedbackSequence) {
        if (feedbackSequence > 0) {
            val sequence = feedbackSequence
            delay(650)
            if (feedbackSequence == sequence) {
                feedbackLabel = null
            }
        }
    }

    fun clearGestureState() {
        startX = null
        startY = null
        endX = null
        endY = null
        pointerCount = 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "盲操模式",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = state.baseUrl.removePrefix("http://").removePrefix("https://"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        FilledIconButton(onClick = onToggleTheme) {
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
                    ControlModeSwitcher(
                        selectedMode = state.currentControlMode,
                        onModeSelected = onModeSelected,
                        enabled = !state.isBusy,
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { gestureAreaSize = it }
                    .pointerInteropFilter { event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                pointerCount = 1
                                startX = event.x
                                startY = event.y
                                endX = event.x
                                endY = event.y
                            }

                            MotionEvent.ACTION_POINTER_DOWN -> {
                                pointerCount = maxOf(pointerCount, event.pointerCount)
                            }

                            MotionEvent.ACTION_MOVE -> {
                                endX = event.getX(0)
                                endY = event.getY(0)
                                pointerCount = maxOf(pointerCount, event.pointerCount)
                            }

                            MotionEvent.ACTION_UP -> {
                                endX = event.x
                                endY = event.y
                                val startGestureX = startX
                                val startGestureY = startY
                                val endGestureX = endX
                                val endGestureY = endY
                                if (
                                    startGestureX != null &&
                                    startGestureY != null &&
                                    endGestureX != null &&
                                    endGestureY != null
                                ) {
                                    val gestureAction = detector.detect(
                                        SwipeGestureInput(
                                            pointerCount = pointerCount,
                                            startX = startGestureX,
                                            startY = startGestureY,
                                            endX = endGestureX,
                                            endY = endGestureY,
                                            viewportWidth = gestureAreaSize.width.toFloat(),
                                            viewportHeight = gestureAreaSize.height.toFloat(),
                                        ),
                                    )
                                    if (gestureAction != null) {
                                        onAction(gestureAction)
                                        feedbackLabel = if (gestureAction == RemoteAction.NextPage) {
                                            "下一页"
                                        } else {
                                            "上一页"
                                        }
                                        feedbackSequence += 1
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                                clearGestureState()
                            }

                            MotionEvent.ACTION_CANCEL -> {
                                clearGestureState()
                            }
                        }
                        true
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f),
                                ),
                            ),
                        )
                        .padding(20.dp),
                ) {
                    Text(
                        text = "向左或向上\n上一页",
                        modifier = Modifier.align(Alignment.TopStart),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "向右或向下\n下一页",
                        modifier = Modifier.align(Alignment.BottomEnd),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )

                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            shadowElevation = 12.dp,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TouchApp,
                                    contentDescription = "盲操手势区",
                                    modifier = Modifier.size(42.dp),
                                )
                            }
                        }
                        Text(
                            text = "整块区域可滑动翻页",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "避开边缘开始或结束，更不容易和系统返回手势冲突。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center,
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = feedbackLabel != null,
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 16.dp,
                        ) {
                            Text(
                                text = feedbackLabel.orEmpty(),
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }

            Text(
                text = state.statusMessage.ifBlank { "盲操已就绪" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
