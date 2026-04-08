package io.github.hugo1120.koreaderremote.platform.input

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PageTurnRateLimiterTest {
    @Test
    fun `allows first page turn immediately`() {
        val limiter = PageTurnRateLimiter(
            minimumIntervalMillis = 100L,
            nowMillis = { 1_000L },
        )

        val allowed = limiter.tryAcquire()

        assertThat(allowed).isTrue()
    }

    @Test
    fun `blocks page turn inside minimum interval`() {
        var now = 1_000L
        val limiter = PageTurnRateLimiter(
            minimumIntervalMillis = 100L,
            nowMillis = { now },
        )

        assertThat(limiter.tryAcquire()).isTrue()
        now = 1_050L

        val allowed = limiter.tryAcquire()

        assertThat(allowed).isFalse()
    }

    @Test
    fun `allows page turn again after minimum interval`() {
        var now = 1_000L
        val limiter = PageTurnRateLimiter(
            minimumIntervalMillis = 100L,
            nowMillis = { now },
        )

        assertThat(limiter.tryAcquire()).isTrue()
        now = 1_100L

        val allowed = limiter.tryAcquire()

        assertThat(allowed).isTrue()
    }
}
