package io.github.hugo1120.koreaderremote.data.repository

import io.github.hugo1120.koreaderremote.data.network.KoreaderHttpClient
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import java.io.IOException
import java.io.InputStream
import okhttp3.OkHttpClient

interface KoreaderRemoteRepository {
    suspend fun connect(rawInput: String): Result<String>

    suspend fun send(baseUrl: String, action: RemoteAction): Result<Unit>

    suspend fun setRotation(baseUrl: String, rotationMode: Int): Result<Unit>

    suspend fun openScreenshotStream(baseUrl: String): InputStream

    suspend fun <T> useScreenshotStream(baseUrl: String, block: (InputStream) -> T): Result<T> =
        runCatching {
            openScreenshotStream(baseUrl).use(block)
        }
}

class HttpKoreaderRemoteRepository(
    private val okHttpClient: OkHttpClient,
) : KoreaderRemoteRepository {
    override suspend fun connect(rawInput: String): Result<String> = runCatching {
        val normalizedBaseUrl = KoreaderHttpClient.normalizeBaseUrl(rawInput)
        val isConnected = KoreaderHttpClient(okHttpClient, normalizedBaseUrl).ping()
        check(isConnected) { "connect failed" }
        normalizedBaseUrl
    }

    override suspend fun send(baseUrl: String, action: RemoteAction): Result<Unit> = runCatching {
        val normalizedBaseUrl = KoreaderHttpClient.normalizeBaseUrl(baseUrl)
        val isSent = KoreaderHttpClient(okHttpClient, normalizedBaseUrl).send(action)
        check(isSent) { "send action failed: $action" }
    }

    override suspend fun setRotation(baseUrl: String, rotationMode: Int): Result<Unit> = runCatching {
        val normalizedBaseUrl = KoreaderHttpClient.normalizeBaseUrl(baseUrl)
        val isRotated = KoreaderHttpClient(okHttpClient, normalizedBaseUrl).setRotation(rotationMode)
        check(isRotated) { "set rotation failed: $rotationMode" }
    }

    override suspend fun openScreenshotStream(baseUrl: String): InputStream {
        val normalizedBaseUrl = KoreaderHttpClient.normalizeBaseUrl(baseUrl)
        return KoreaderHttpClient(okHttpClient, normalizedBaseUrl).openScreenshotStream()
    }

    override suspend fun <T> useScreenshotStream(
        baseUrl: String,
        block: (InputStream) -> T,
    ): Result<T> = runCatching {
        openScreenshotStream(baseUrl).use(block)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = {
            Result.failure(
                IOException("screenshot operation failed", it),
            )
        },
    )
}
