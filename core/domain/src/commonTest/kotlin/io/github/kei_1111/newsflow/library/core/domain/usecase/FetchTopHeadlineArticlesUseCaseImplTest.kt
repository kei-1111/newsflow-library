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

class FetchTopHeadlineArticlesUseCaseImplTest {

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val articles = createTestArticles(3)
        everySuspend { newsRepository.fetchArticles(any(), any()) } returns Result.success(articles)
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isSuccess)
        assertEquals(articles, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network error")
        everySuspend { newsRepository.fetchArticles(any(), any()) } returns Result.failure(error)
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.NetworkFailure>(exception)
        assertEquals("Network error", exception.message)
    }

    @Test
    fun `invoke propagates Unauthorized error from repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.Unauthorized("Invalid API key")
        everySuspend { newsRepository.fetchArticles(any(), any()) } returns Result.failure(error)
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.NetworkError.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates RateLimitExceeded error from repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.RateLimitExceeded("Rate limit exceeded")
        everySuspend { newsRepository.fetchArticles(any(), any()) } returns Result.failure(error)
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.NetworkError.RateLimitExceeded>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates ServerError from repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.ServerError("Internal server error")
        everySuspend { newsRepository.fetchArticles(any(), any()) } returns Result.failure(error)
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.NetworkError.ServerError>(result.exceptionOrNull())
    }

    @Test
    fun `invoke passes forceRefresh parameter to repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val articles = createTestArticles(1)
        everySuspend { newsRepository.fetchArticles(any(), any()) } returns Result.success(articles)
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        useCase("technology", forceRefresh = true)

        verifySuspend { newsRepository.fetchArticles("technology", true) }
    }

    @Test
    fun `invoke uses default forceRefresh false when not specified`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val articles = createTestArticles(1)
        everySuspend { newsRepository.fetchArticles(any(), any()) } returns Result.success(articles)
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        useCase("technology")

        verifySuspend { newsRepository.fetchArticles("technology", false) }
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
