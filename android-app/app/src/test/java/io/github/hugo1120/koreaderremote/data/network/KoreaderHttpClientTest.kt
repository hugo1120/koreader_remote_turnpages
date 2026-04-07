package io.github.hugo1120.koreaderremote.data.network

import com.google.common.truth.Truth.assertThat
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class KoreaderHttpClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `ping uses goto view rel zero endpoint`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))
        val client = KoreaderHttpClient(
            okHttpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
        )

        val result = client.ping()

        assertThat(result).isTrue()
        assertThat(server.takeRequest().path).isEqualTo("/koreader/event/GotoViewRel/0")
    }

    @Test
    fun `normalizes raw host to default koreader port`() {
        val normalized = KoreaderHttpClient.normalizeBaseUrl("192.168.1.88")

        assertThat(normalized).isEqualTo("http://192.168.1.88:8080")
    }

    @Test
    fun `normalizes host with existing scheme without duplicating scheme`() {
        val normalized = KoreaderHttpClient.normalizeBaseUrl("https://192.168.1.88")

        assertThat(normalized).isEqualTo("https://192.168.1.88:8080")
    }

    @Test
    fun `normalizes host with existing port without overriding port`() {
        val normalized = KoreaderHttpClient.normalizeBaseUrl("192.168.1.88:18080")

        assertThat(normalized).isEqualTo("http://192.168.1.88:18080")
    }

    @Test
    fun `next page uses plus one endpoint`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))
        val client = KoreaderHttpClient(
            okHttpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
        )

        val result = client.send(RemoteAction.NextPage)

        assertThat(result).isTrue()
        assertThat(server.takeRequest().path).isEqualTo("/koreader/event/GotoViewRel/1")
    }

    @Test
    fun `open screenshot stream uses screen bb endpoint`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("fake-png"),
        )
        val client = KoreaderHttpClient(
            okHttpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
        )

        val body = client.openScreenshotStream().use { it.readBytes().decodeToString() }

        assertThat(body).isEqualTo("fake-png")
        assertThat(server.takeRequest().path).isEqualTo("/koreader/device/screen/bb")
    }
}
