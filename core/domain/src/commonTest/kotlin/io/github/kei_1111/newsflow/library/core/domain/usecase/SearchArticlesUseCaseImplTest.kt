package io.github.kei_1111.newsflow.library.core.domain.usecase

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SearchArticlesUseCaseImplTest {

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val articles = createTestArticles(3)
        everySuspend {
            newsRepository.searchArticles(any(), any(), any(), any(), any())
        } returns Result.success(articles)
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        val result = useCase("kotlin")

        assertTrue(result.isSuccess)
        assertEquals(articles, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network error")
        everySuspend {
            newsRepository.searchArticles(any(), any(), any(), any(), any())
        } returns Result.failure(error)
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        val result = useCase("kotlin")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.NetworkFailure>(exception)
        assertEquals("Network error", exception.message)
    }

    @Test
    fun `invoke returns InvalidParameter error when query is empty`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        val result = useCase("")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NewsflowError.InternalError.InvalidParameter>(exception)
        assertEquals("Search query cannot be empty", exception.message)
    }

    @Test
    fun `invoke returns InvalidParameter error when query is blank`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        val result = useCase("   ")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NewsflowError.InternalError.InvalidParameter>(exception)
        assertEquals("Search query cannot be empty", exception.message)
    }

    @Test
    fun `invoke trims query before passing to repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val articles = createTestArticles(1)
        everySuspend {
            newsRepository.searchArticles(any(), any(), any(), any(), any())
        } returns Result.success(articles)
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        useCase("  kotlin  ")

        verifySuspend { newsRepository.searchArticles("kotlin", null, null, null, null) }
    }

    @Test
    fun `invoke passes all parameters to repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val articles = createTestArticles(1)
        everySuspend {
            newsRepository.searchArticles(any(), any(), any(), any(), any())
        } returns Result.success(articles)
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        useCase(
            query = "kotlin",
            sortBy = "publishedAt",
            from = "2025-01-01",
            to = "2025-01-31",
            language = "en",
        )

        verifySuspend {
            newsRepository.searchArticles("kotlin", "publishedAt", "2025-01-01", "2025-01-31", "en")
        }
    }

    @Test
    fun `invoke propagates Unauthorized error from repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.Unauthorized("Invalid API key")
        everySuspend {
            newsRepository.searchArticles(any(), any(), any(), any(), any())
        } returns Result.failure(error)
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        val result = useCase("kotlin")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.NetworkError.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates RateLimitExceeded error from repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.RateLimitExceeded("Rate limit exceeded")
        everySuspend {
            newsRepository.searchArticles(any(), any(), any(), any(), any())
        } returns Result.failure(error)
        val useCase = SearchArticlesUseCaseImpl(newsRepository)

        val result = useCase("kotlin")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.NetworkError.RateLimitExceeded>(result.exceptionOrNull())
    }

    private fun createTestArticle(index: Int, prefix: String = "Test") = Article(
        id = "$index",
        source = "$prefix Source $index",
        author = "$prefix Author $index",
        title = "$prefix Title $index",
        description = "$prefix Description $index",
        url = "https://example.com/$prefix-$index",
        imageUrl = "https://example.com/image-$index.jpg",
        publishedAt = 1234567890000L + index,
    )

    private fun createTestArticles(count: Int, prefix: String = "Test") =
        List(count) { createTestArticle(it + 1, prefix) }
}
