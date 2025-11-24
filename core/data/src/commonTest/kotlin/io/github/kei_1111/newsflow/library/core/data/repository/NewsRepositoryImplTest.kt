package io.github.kei_1111.newsflow.library.core.data.repository

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.network.exception.NetworkException
import io.github.kei_1111.newsflow.library.core.network.model.ArticleResponse
import io.github.kei_1111.newsflow.library.core.network.model.NewsResponse
import io.github.kei_1111.newsflow.library.core.network.model.SourceResponse
import io.github.kei_1111.newsflow.library.core.test.network.FakeNewsApiService
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NewsRepositoryImplTest {

    private lateinit var newsApiService: FakeNewsApiService

    @BeforeTest
    fun setup() {
        newsApiService = FakeNewsApiService()
    }

    @Test
    fun `fetchArticles returns success with articles when API call succeeds`() = runTest {
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 2,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Test Source 1"),
                    author = "Test Author 1",
                    title = "Test Title 1",
                    description = "Test Description 1",
                    url = "https://example.com/1",
                    urlToImage = "https://example.com/image1.jpg",
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = "Test Content 1",
                ),
                ArticleResponse(
                    source = SourceResponse(id = "test-id", name = "Test Source 2"),
                    author = null,
                    title = "Test Title 2",
                    description = null,
                    url = "https://example.com/2",
                    urlToImage = null,
                    publishedAt = "2024-01-02T00:00:00Z",
                    content = null,
                ),
            ),
        )
        newsApiService.setResult(Result.success(newsResponse))
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isSuccess)
        val articles = result.getOrNull()!!
        assertEquals(2, articles.size)
        assertEquals("Test Source 1", articles[0].source)
        assertEquals("Test Title 1", articles[0].title)
        assertEquals("Test Source 2", articles[1].source)
        assertEquals("Test Title 2", articles[1].title)
    }

    @Test
    fun `fetchArticles returns failure with Unauthorized when API returns Unauthorized`() = runTest {
        val exception = NetworkException.Unauthorized("Invalid API key")
        newsApiService.setResult(Result.failure(exception))
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.Unauthorized>(error)
        assertEquals("Invalid API key", error.message)
    }

    @Test
    fun `fetchArticles returns failure with RateLimitExceeded when API returns RateLimitExceeded`() = runTest {
        val exception = NetworkException.RateLimitExceeded("Rate limit exceeded")
        newsApiService.setResult(Result.failure(exception))
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.RateLimitExceeded>(error)
        assertEquals("Rate limit exceeded", error.message)
    }

    @Test
    fun `fetchArticles returns failure with BadRequest when API returns BadRequest`() = runTest {
        val exception = NetworkException.BadRequest("Bad request")
        newsApiService.setResult(Result.failure(exception))
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.BadRequest>(error)
        assertEquals("Bad request", error.message)
    }

    @Test
    fun `fetchArticles returns failure with ServerError when API returns ServerError`() = runTest {
        val exception = NetworkException.ServerError("Internal server error")
        newsApiService.setResult(Result.failure(exception))
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.ServerError>(error)
        assertEquals("Internal server error", error.message)
    }

    @Test
    fun `fetchArticles returns failure with NetworkFailure when API returns NetworkFailure`() = runTest {
        val exception = NetworkException.NetworkFailure("Network error")
        newsApiService.setResult(Result.failure(exception))
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkFailure>(error)
        assertEquals("Network error", error.message)
    }
}