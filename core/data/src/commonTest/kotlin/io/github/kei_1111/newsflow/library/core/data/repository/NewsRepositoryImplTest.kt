package io.github.kei_1111.newsflow.library.core.data.repository

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.network.api.NewsApiService
import io.github.kei_1111.newsflow.library.core.network.exception.NetworkException
import io.github.kei_1111.newsflow.library.core.network.model.ArticleResponse
import io.github.kei_1111.newsflow.library.core.network.model.NewsResponse
import io.github.kei_1111.newsflow.library.core.network.model.SourceResponse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NewsRepositoryImplTest {

    @Test
    fun `fetchArticles returns success with articles when API call succeeds`() = runTest {
        val newsApiService = mock<NewsApiService>()
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
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(newsResponse)
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
        val newsApiService = mock<NewsApiService>()
        val exception = NetworkException.Unauthorized("Invalid API key")
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.failure(exception)
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.Unauthorized>(error)
        assertEquals("Invalid API key", error.message)
    }

    @Test
    fun `fetchArticles returns failure with RateLimitExceeded when API returns RateLimitExceeded`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val exception = NetworkException.RateLimitExceeded("Rate limit exceeded")
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.failure(exception)
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.RateLimitExceeded>(error)
        assertEquals("Rate limit exceeded", error.message)
    }

    @Test
    fun `fetchArticles returns failure with BadRequest when API returns BadRequest`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val exception = NetworkException.BadRequest("Bad request")
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.failure(exception)
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.BadRequest>(error)
        assertEquals("Bad request", error.message)
    }

    @Test
    fun `fetchArticles returns failure with ServerError when API returns ServerError`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val exception = NetworkException.ServerError("Internal server error")
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.failure(exception)
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.ServerError>(error)
        assertEquals("Internal server error", error.message)
    }

    @Test
    fun `fetchArticles returns failure with NetworkFailure when API returns NetworkFailure`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val exception = NetworkException.NetworkFailure("Network error")
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.failure(exception)
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.NetworkFailure>(error)
        assertEquals("Network error", error.message)
    }

    // Cache behavior tests
    @Test
    fun `fetchArticles caches result on first call`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Test Source"),
                    author = "Test Author",
                    title = "Test Title",
                    description = "Test Description",
                    url = "https://example.com/1",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(newsResponse)
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.fetchArticles("technology")

        assertTrue(result.isSuccess)
        verifySuspend(exactly(1)) { newsApiService.fetchTopHeadlines(any()) }
    }

    @Test
    fun `fetchArticles returns cached data on second call without forceRefresh`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Test Source"),
                    author = "Test Author",
                    title = "Test Title",
                    description = "Test Description",
                    url = "https://example.com/1",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(newsResponse)
        val repository = NewsRepositoryImpl(newsApiService)

        repository.fetchArticles("technology", forceRefresh = false)
        val result = repository.fetchArticles("technology", forceRefresh = false)

        assertTrue(result.isSuccess)
        verifySuspend(exactly(1)) { newsApiService.fetchTopHeadlines(any()) }
    }

    @Test
    fun `fetchArticles bypasses cache when forceRefresh is true`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Test Source"),
                    author = "Test Author",
                    title = "Test Title",
                    description = "Test Description",
                    url = "https://example.com/1",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(newsResponse)
        val repository = NewsRepositoryImpl(newsApiService)

        repository.fetchArticles("technology", forceRefresh = false)
        val result = repository.fetchArticles("technology", forceRefresh = true)

        assertTrue(result.isSuccess)
        verifySuspend(exactly(2)) { newsApiService.fetchTopHeadlines(any()) }
    }

    @Test
    fun `fetchArticles updates cache when forceRefresh is true`() = runTest {
        val newsApiService = mock<NewsApiService>(MockMode.autoUnit)
        val firstResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Old Source"),
                    author = "Old Author",
                    title = "Old Title",
                    description = "Old Description",
                    url = "https://example.com/old",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        val secondResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "New Source"),
                    author = "New Author",
                    title = "New Title",
                    description = "New Description",
                    url = "https://example.com/new",
                    urlToImage = null,
                    publishedAt = "2024-01-02T00:00:00Z",
                    content = null,
                ),
            ),
        )
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(firstResponse)
        val repository = NewsRepositoryImpl(newsApiService)

        repository.fetchArticles("technology", forceRefresh = false)
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(secondResponse)
        repository.fetchArticles("technology", forceRefresh = true)
        val result = repository.fetchArticles("technology", forceRefresh = false)

        assertTrue(result.isSuccess)
        val articles = result.getOrNull()!!
        assertEquals("New Title", articles[0].title)
        verifySuspend(exactly(2)) { newsApiService.fetchTopHeadlines(any()) }
    }

    // Cache isolation tests
    @Test
    fun `different categories maintain separate caches`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val techResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Tech Source"),
                    author = "Tech Author",
                    title = "Tech Title",
                    description = "Tech Description",
                    url = "https://example.com/tech",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        val businessResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Business Source"),
                    author = "Business Author",
                    title = "Business Title",
                    description = "Business Description",
                    url = "https://example.com/business",
                    urlToImage = null,
                    publishedAt = "2024-01-02T00:00:00Z",
                    content = null,
                ),
            ),
        )
        val repository = NewsRepositoryImpl(newsApiService)

        everySuspend { newsApiService.fetchTopHeadlines("technology") } returns Result.success(techResponse)
        val techResult1 = repository.fetchArticles("technology")
        everySuspend { newsApiService.fetchTopHeadlines("business") } returns Result.success(businessResponse)
        val businessResult = repository.fetchArticles("business")
        val techResult2 = repository.fetchArticles("technology")

        assertTrue(techResult1.isSuccess)
        assertTrue(businessResult.isSuccess)
        assertTrue(techResult2.isSuccess)
        assertEquals("Tech Title", techResult1.getOrNull()!![0].title)
        assertEquals("Business Title", businessResult.getOrNull()!![0].title)
        assertEquals("Tech Title", techResult2.getOrNull()!![0].title)
        verifySuspend(exactly(1)) { newsApiService.fetchTopHeadlines("technology") }
        verifySuspend(exactly(1)) { newsApiService.fetchTopHeadlines("business") }
    }

    @Test
    fun `fetchArticles clears cache when API fails with forceRefresh`() = runTest {
        val newsApiService = mock<NewsApiService>(MockMode.autoUnit)
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Test Source"),
                    author = "Test Author",
                    title = "Test Title",
                    description = "Test Description",
                    url = "https://example.com/1",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(newsResponse)
        val repository = NewsRepositoryImpl(newsApiService)

        repository.fetchArticles("technology", forceRefresh = false)
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns
            Result.failure(NetworkException.NetworkFailure("Network error"))
        val failedResult = repository.fetchArticles("technology", forceRefresh = true)
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(newsResponse)
        val result = repository.fetchArticles("technology", forceRefresh = false)

        assertTrue(failedResult.isFailure)
        assertTrue(result.isSuccess)
        verifySuspend(exactly(3)) { newsApiService.fetchTopHeadlines(any()) }
    }

    // getArticleById tests
    @Test
    fun `getArticleById returns article when found in cache`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Test Source"),
                    author = "Test Author",
                    title = "Test Title",
                    description = "Test Description",
                    url = "https://example.com/1",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        everySuspend { newsApiService.fetchTopHeadlines(any()) } returns Result.success(newsResponse)
        val repository = NewsRepositoryImpl(newsApiService)

        repository.fetchArticles("technology")
        val articleId = "https://example.com/1".hashCode().toString()
        val result = repository.getArticleById(articleId)

        assertTrue(result.isSuccess)
        val article = result.getOrNull()!!
        assertEquals("Test Title", article.title)
    }

    @Test
    fun `getArticleById returns failure when article not in cache`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.getArticleById("nonexistent-id")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.InternalError.ArticleNotFound>(result.exceptionOrNull())
    }

    @Test
    fun `getArticleById searches across all cached categories`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val techResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Tech Source"),
                    author = "Tech Author",
                    title = "Tech Title",
                    description = "Tech Description",
                    url = "https://example.com/tech",
                    urlToImage = null,
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = null,
                ),
            ),
        )
        val businessResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Business Source"),
                    author = "Business Author",
                    title = "Business Title",
                    description = "Business Description",
                    url = "https://example.com/business",
                    urlToImage = null,
                    publishedAt = "2024-01-02T00:00:00Z",
                    content = null,
                ),
            ),
        )
        val repository = NewsRepositoryImpl(newsApiService)

        everySuspend { newsApiService.fetchTopHeadlines("technology") } returns Result.success(techResponse)
        repository.fetchArticles("technology")
        everySuspend { newsApiService.fetchTopHeadlines("business") } returns Result.success(businessResponse)
        repository.fetchArticles("business")

        val techArticleId = "https://example.com/tech".hashCode().toString()
        val businessArticleId = "https://example.com/business".hashCode().toString()

        val techResult = repository.getArticleById(techArticleId)
        val businessResult = repository.getArticleById(businessArticleId)

        assertTrue(techResult.isSuccess)
        assertTrue(businessResult.isSuccess)
        assertEquals("Tech Title", techResult.getOrNull()!!.title)
        assertEquals("Business Title", businessResult.getOrNull()!!.title)
    }

    @Test
    fun `getArticleById returns failure when cache is empty`() = runTest {
        val newsApiService = mock<NewsApiService>()
        val repository = NewsRepositoryImpl(newsApiService)

        val result = repository.getArticleById("any-id")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.InternalError.ArticleNotFound>(result.exceptionOrNull())
    }
}
