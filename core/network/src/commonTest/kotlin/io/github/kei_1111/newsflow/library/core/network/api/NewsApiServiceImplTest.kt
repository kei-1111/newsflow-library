package io.github.kei_1111.newsflow.library.core.network.api

import io.github.kei_1111.newsflow.library.core.network.core.defaultConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewsApiServiceImplTest {

    @Test
    fun `fetchTopHeadlines returns success when API returns valid response`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("https://newsapi.org/v2/top-headlines", request.url.toString().substringBefore('?'))
            respond(
                content = ByteReadChannel(VALID_RESPONSE_JSON),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) { defaultConfig() }
        val service = NewsApiServiceImpl(client)

        val result = service.fetchTopHeadlines("technology")

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals("ok", response?.status)
        assertEquals(2, response?.articles?.size)
    }

    @Test
    fun `fetchTopHeadlines returns failure when API returns 401 Unauthorized`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel(
                    """{"status":"error","code":"apiKeyInvalid","message":"Your API key is invalid"}"""
                ),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) { defaultConfig() }
        val service = NewsApiServiceImpl(client)

        val result = service.fetchTopHeadlines("technology")

        assertTrue(result.isFailure)
        assertEquals("Invalid API key", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchTopHeadlines returns failure when API returns 429 Rate Limit`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel(
                    """{"status":"error","code":"rateLimited","message":"You have made too many requests"}"""
                ),
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) { defaultConfig() }
        val service = NewsApiServiceImpl(client)

        val result = service.fetchTopHeadlines("technology")

        assertTrue(result.isFailure)
        assertEquals("Rate limit exceeded", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchTopHeadlines returns failure when API returns 500 Server Error`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel("""{"status":"error","message":"Internal server error"}"""),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) { defaultConfig() }
        val service = NewsApiServiceImpl(client)

        val result = service.fetchTopHeadlines("technology")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Server error") == true)
    }

    @Test
    fun `fetchTopHeadlines includes correct headers and parameters`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("technology", request.url.parameters["category"])
            assertEquals("us", request.url.parameters["country"])
            assertTrue(request.headers.contains("X-Api-Key"))

            respond(
                content = ByteReadChannel(VALID_RESPONSE_JSON),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) { defaultConfig() }
        val service = NewsApiServiceImpl(client)

        service.fetchTopHeadlines("technology")
    }

    companion object {
        private val VALID_RESPONSE_JSON = """
            {
                "status": "ok",
                "totalResults": 2,
                "articles": [
                    {
                        "source": {"id": null, "name": "Test Source 1"},
                        "author": "Test Author 1",
                        "title": "Test Article 1",
                        "description": "Test Description 1",
                        "url": "https://example.com/1",
                        "urlToImage": "https://example.com/image1.jpg",
                        "publishedAt": "2024-01-01T00:00:00Z",
                        "content": "Test Content 1"
                    },
                    {
                        "source": {"id": "test-source", "name": "Test Source 2"},
                        "author": null,
                        "title": "Test Article 2",
                        "description": null,
                        "url": "https://example.com/2",
                        "urlToImage": null,
                        "publishedAt": "2024-01-02T00:00:00Z",
                        "content": null
                    }
                ]
            }
        """.trimIndent()
    }
}
