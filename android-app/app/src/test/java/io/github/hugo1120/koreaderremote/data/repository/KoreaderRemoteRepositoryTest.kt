package io.github.hugo1120.koreaderremote.data.repository

import com.google.common.truth.Truth.assertThat
import io.github.hugo1120.koreaderremote.domain.model.RemoteAction
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class KoreaderRemoteRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: KoreaderRemoteRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = HttpKoreaderRemoteRepository(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `connect returns normalized base url when ping succeeds`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200))

        val result = repository.connect("${server.hostName}:${server.port}")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("http://${server.hostName}:${server.port}")
        assertThat(server.takeRequest().path).isEqualTo("/koreader/event/GotoViewRel/0")
    }

    @Test
    fun `send returns failure result when endpoint fails`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.send(
            baseUrl = server.url("/").toString(),
            action = RemoteAction.NextPage,
        )

        assertThat(result.isFailure).isTrue()
        assertThat(server.takeRequest().path).isEqualTo("/koreader/event/GotoViewRel/1")
    }
}
