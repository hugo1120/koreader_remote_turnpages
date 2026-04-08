package io.github.hugo1120.koreaderremote.platform.input

import com.google.common.truth.Truth.assertThat
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import org.junit.Test

class SwipePageTurnDetectorTest {
    private val detector = SwipePageTurnDetector(
        minimumDistancePx = 48f,
        edgeSafeInsetPx = 16f,
    )

    @Test
    fun `swipe right maps to next page`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 64f,
                startY = 120f,
                endX = 180f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isEqualTo(RemoteAction.NextPage)
    }

    @Test
    fun `swipe down maps to next page`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 120f,
                startY = 100f,
                endX = 120f,
                endY = 220f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isEqualTo(RemoteAction.NextPage)
    }

    @Test
    fun `swipe left maps to previous page`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 200f,
                startY = 120f,
                endX = 80f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isEqualTo(RemoteAction.PreviousPage)
    }

    @Test
    fun `swipe up maps to previous page`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 120f,
                startY = 220f,
                endX = 120f,
                endY = 100f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isEqualTo(RemoteAction.PreviousPage)
    }

    @Test
    fun `movement below threshold does not trigger page turn`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 120f,
                startY = 120f,
                endX = 150f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isNull()
    }

    @Test
    fun `multi pointer gesture is ignored`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 2,
                startX = 64f,
                startY = 120f,
                endX = 180f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isNull()
    }

    @Test
    fun `视口宽高不合法时返回 null`() {
        val invalidWidthAction = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 64f,
                startY = 120f,
                endX = 180f,
                endY = 120f,
                viewportWidth = 0f,
                viewportHeight = 640f,
            ),
        )
        val invalidHeightAction = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 64f,
                startY = 120f,
                endX = 180f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = -1f,
            ),
        )

        assertThat(invalidWidthAction).isNull()
        assertThat(invalidHeightAction).isNull()
    }

    @Test
    fun `swipe touching edge safe zone is ignored`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 8f,
                startY = 120f,
                endX = 180f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isNull()
    }

    @Test
    fun `终点落在边缘安全区时返回 null`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 64f,
                startY = 120f,
                endX = 8f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isNull()
    }

    @Test
    fun `swipe inside safe zone is accepted`() {
        val action = detector.detect(
            SwipeGestureInput(
                pointerCount = 1,
                startX = 32f,
                startY = 120f,
                endX = 180f,
                endY = 120f,
                viewportWidth = 360f,
                viewportHeight = 640f,
            ),
        )

        assertThat(action).isEqualTo(RemoteAction.NextPage)
    }
}
