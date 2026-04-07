package io.github.hugo1120.koreaderremote.data.network

import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class KoreaderHttpClient(
    private val okHttpClient: OkHttpClient,
    baseUrl: String,
) {
    private val normalizedBaseUrl = normalizeBaseUrl(baseUrl)

    suspend fun ping(): Boolean = execute(PING_PATH)

    suspend fun send(action: RemoteAction): Boolean = execute(action.endpointPath)

    suspend fun setRotation(rotationMode: Int): Boolean = execute("$SET_ROTATION_PREFIX/$rotationMode")

    suspend fun openScreenshotStream(): InputStream = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(buildUrl(SCREENSHOT_PATH))
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            throw IOException("screenshot request failed with code ${response.code}")
        }

        val body = response.body
        if (body == null) {
            response.close()
            throw IOException("screenshot response body is empty")
        }

        val stream = body.byteStream()
        object : FilterInputStream(stream) {
            override fun close() {
                try {
                    super.close()
                } finally {
                    response.close()
                }
            }
        }
    }

    private suspend fun execute(path: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(buildUrl(path))
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }

    private fun buildUrl(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return normalizedBaseUrl + normalizedPath
    }

    companion object {
        private const val DEFAULT_KOREADER_PORT = 8080
        private val SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z\\d+\\-.]*://")
        private const val PING_PATH = "/koreader/event/GotoViewRel/0"
        private const val SET_ROTATION_PREFIX = "/koreader/event/SetRotationMode"
        private const val SCREENSHOT_PATH = "/koreader/device/screen/bb"

        fun normalizeBaseUrl(input: String): String {
            val rawInput = input.trim()
            require(rawInput.isNotEmpty()) { "base url is blank" }

            val withScheme = if (SCHEME_REGEX.containsMatchIn(rawInput)) rawInput else "http://$rawInput"
            val uri = try {
                URI(withScheme)
            } catch (exception: URISyntaxException) {
                throw IllegalArgumentException("invalid base url: $input", exception)
            }

            val host = uri.host ?: extractHostFromAuthority(uri.rawAuthority)
            require(!host.isNullOrBlank()) { "invalid base url: $input" }

            val scheme = (uri.scheme ?: "http").lowercase()
            val port = if (uri.port == -1) DEFAULT_KOREADER_PORT else uri.port
            val formattedHost = if (host.contains(":") && !host.startsWith("[")) "[$host]" else host
            return "$scheme://$formattedHost:$port"
        }

        private fun extractHostFromAuthority(authority: String?): String? {
            if (authority.isNullOrBlank()) return null

            val withoutUserInfo = authority.substringAfterLast("@")
            return if (withoutUserInfo.startsWith("[")) {
                withoutUserInfo.substringAfter("[").substringBefore("]")
            } else {
                withoutUserInfo.substringBefore(":")
            }
        }
    }
}
