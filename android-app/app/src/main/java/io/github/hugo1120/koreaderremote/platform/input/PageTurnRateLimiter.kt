package io.github.hugo1120.koreaderremote.platform.input

class PageTurnRateLimiter(
    private val minimumIntervalMillis: Long = 100L,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) {
    private var lastAcquiredAtMillis = Long.MIN_VALUE

    fun tryAcquire(): Boolean {
        val now = nowMillis()
        if (lastAcquiredAtMillis != Long.MIN_VALUE && now - lastAcquiredAtMillis < minimumIntervalMillis) {
            return false
        }

        lastAcquiredAtMillis = now
        return true
    }
}
