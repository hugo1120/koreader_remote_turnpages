package io.github.hugo1120.koreaderremote.platform.input

import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import kotlin.math.abs
import kotlin.math.max

data class SwipeGestureInput(
    val pointerCount: Int,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val viewportWidth: Float,
    val viewportHeight: Float,
)

class SwipePageTurnDetector(
    private val minimumDistancePx: Float = 72f,
    private val edgeSafeInsetPx: Float = 24f,
) {
    fun detect(input: SwipeGestureInput): RemoteAction? {
        if (input.pointerCount != 1) {
            return null
        }

        if (input.viewportWidth <= 0f || input.viewportHeight <= 0f) {
            return null
        }

        if (!isInsideSafeZone(input.startX, input.startY, input.viewportWidth, input.viewportHeight)) {
            return null
        }

        if (!isInsideSafeZone(input.endX, input.endY, input.viewportWidth, input.viewportHeight)) {
            return null
        }

        val deltaX = input.endX - input.startX
        val deltaY = input.endY - input.startY
        if (max(abs(deltaX), abs(deltaY)) < minimumDistancePx) {
            return null
        }

        return if (abs(deltaX) >= abs(deltaY)) {
            if (deltaX >= 0f) {
                RemoteAction.NextPage
            } else {
                RemoteAction.PreviousPage
            }
        } else {
            if (deltaY >= 0f) {
                RemoteAction.NextPage
            } else {
                RemoteAction.PreviousPage
            }
        }
    }

    private fun isInsideSafeZone(
        x: Float,
        y: Float,
        viewportWidth: Float,
        viewportHeight: Float,
    ): Boolean {
        val minX = edgeSafeInsetPx
        val maxX = viewportWidth - edgeSafeInsetPx
        val minY = edgeSafeInsetPx
        val maxY = viewportHeight - edgeSafeInsetPx
        return x in minX..maxX && y in minY..maxY
    }
}
