package io.github.kei_1111.newsflow.library.core.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.network.api.GeminiApiService
import io.github.kei_1111.newsflow.library.core.network.exception.GeminiException
import io.github.kei_1111.newsflow.library.core.network.exception.NetworkException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryRepositoryImplTest {

    @Test
    fun `summarizeArticle emits text chunks from API stream`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns
            flowOf("Hello ", "World", "!")
        val repository = SummaryRepositoryImpl(geminiApiService)

        repository.summarizeArticle("https://example.com/article").test {
            assertEquals("Hello ", awaitItem())
            assertEquals("World", awaitItem())
            assertEquals("!", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `summarizeArticle returns cached summary on second call`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns
            flowOf("Cached ", "Summary")
        val repository = SummaryRepositoryImpl(geminiApiService)

        // First call - should hit API
        repository.summarizeArticle("https://example.com/article").test {
            assertEquals("Cached ", awaitItem())
            assertEquals("Summary", awaitItem())
            awaitComplete()
        }

        // Second call - should return cached result
        repository.summarizeArticle("https://example.com/article").test {
            assertEquals("Cached Summary", awaitItem())
            awaitComplete()
        }

        verify(exactly(1)) { geminiApiService.summarizeUrlStream("https://example.com/article") }
    }

    @Test
    fun `summarizeArticle caches separately for different URLs`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        every { geminiApiService.summarizeUrlStream("https://example.com/article1") } returns
            flowOf("Summary 1")
        every { geminiApiService.summarizeUrlStream("https://example.com/article2") } returns
            flowOf("Summary 2")
        val repository = SummaryRepositoryImpl(geminiApiService)

        // First URL
        repository.summarizeArticle("https://example.com/article1").test {
            assertEquals("Summary 1", awaitItem())
            awaitComplete()
        }

        // Second URL
        repository.summarizeArticle("https://example.com/article2").test {
            assertEquals("Summary 2", awaitItem())
            awaitComplete()
        }

        // Both should be cached
        repository.summarizeArticle("https://example.com/article1").test {
            assertEquals("Summary 1", awaitItem())
            awaitComplete()
        }

        repository.summarizeArticle("https://example.com/article2").test {
            assertEquals("Summary 2", awaitItem())
            awaitComplete()
        }

        verify(exactly(1)) { geminiApiService.summarizeUrlStream("https://example.com/article1") }
        verify(exactly(1)) { geminiApiService.summarizeUrlStream("https://example.com/article2") }
    }

    @Test
    fun `summarizeArticle converts Unauthorized exception to NetworkError`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns flow {
            throw NetworkException.Unauthorized("Invalid API key")
        }
        val repository = SummaryRepositoryImpl(geminiApiService)

        repository.summarizeArticle("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.Unauthorized>(error)
            assertEquals("Invalid API key", error.message)
        }
    }

    @Test
    fun `summarizeArticle converts RateLimitExceeded exception to NetworkError`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns flow {
            throw NetworkException.RateLimitExceeded("Rate limit exceeded")
        }
        val repository = SummaryRepositoryImpl(geminiApiService)

        repository.summarizeArticle("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.RateLimitExceeded>(error)
            assertEquals("Rate limit exceeded", error.message)
        }
    }

    @Test
    fun `summarizeArticle converts ContentFiltered exception to AIError`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns flow {
            throw GeminiException.ContentFiltered("Content filtered")
        }
        val repository = SummaryRepositoryImpl(geminiApiService)

        repository.summarizeArticle("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.ContentFiltered>(error)
            assertEquals("Content filtered", error.message)
        }
    }

    @Test
    fun `summarizeArticle converts ServerError exception to NetworkError`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns flow {
            throw NetworkException.ServerError("Server error")
        }
        val repository = SummaryRepositoryImpl(geminiApiService)

        repository.summarizeArticle("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.ServerError>(error)
            assertEquals("Server error", error.message)
        }
    }

    @Test
    fun `summarizeArticle does not cache on error`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        var callCount = 0
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns flow {
            callCount++
            if (callCount == 1) {
                throw NetworkException.ServerError("First call fails")
            } else {
                emit("Success on retry")
            }
        }
        val repository = SummaryRepositoryImpl(geminiApiService)

        // First call - fails
        repository.summarizeArticle("https://example.com/article").test {
            awaitError()
        }

        // Second call - should hit API again (not cached)
        repository.summarizeArticle("https://example.com/article").test {
            assertEquals("Success on retry", awaitItem())
            awaitComplete()
        }

        verify(exactly(2)) { geminiApiService.summarizeUrlStream("https://example.com/article") }
    }

    @Test
    fun `summarizeArticle does not cache empty response`() = runTest {
        val geminiApiService = mock<GeminiApiService>()
        var callCount = 0
        every { geminiApiService.summarizeUrlStream("https://example.com/article") } returns flow {
            callCount++
            if (callCount > 1) {
                emit("Non-empty response")
            }
            // First call emits nothing
        }
        val repository = SummaryRepositoryImpl(geminiApiService)

        // First call - empty response
        repository.summarizeArticle("https://example.com/article").test {
            awaitComplete()
        }

        // Second call - should hit API again (not cached due to empty)
        repository.summarizeArticle("https://example.com/article").test {
            assertEquals("Non-empty response", awaitItem())
            awaitComplete()
        }

        verify(exactly(2)) { geminiApiService.summarizeUrlStream("https://example.com/article") }
    }
}
