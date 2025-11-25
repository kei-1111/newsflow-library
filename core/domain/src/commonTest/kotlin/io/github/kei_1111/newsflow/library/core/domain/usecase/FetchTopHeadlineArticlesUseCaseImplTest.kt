package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticles
import io.github.kei_1111.newsflow.library.core.test.repository.FakeNewsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FetchTopHeadlineArticlesUseCaseImplTest {

    private lateinit var newsRepository: FakeNewsRepository

    @BeforeTest
    fun setup() {
        newsRepository = FakeNewsRepository()
    }

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val articles = createTestArticles(3)
        newsRepository.setFetchResult(Result.success(articles))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isSuccess)
        assertEquals(articles, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val error = NewsflowError.NetworkFailure("Network error")
        newsRepository.setFetchResult(Result.failure(error))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkFailure>(exception)
        assertEquals("Network error", exception.message)
    }

    @Test
    fun `invoke propagates Unauthorized error from repository`() = runTest {
        val error = NewsflowError.Unauthorized("Invalid API key")
        newsRepository.setFetchResult(Result.failure(error))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates RateLimitExceeded error from repository`() = runTest {
        val error = NewsflowError.RateLimitExceeded("Rate limit exceeded")
        newsRepository.setFetchResult(Result.failure(error))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.RateLimitExceeded>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates ServerError from repository`() = runTest {
        val error = NewsflowError.ServerError("Internal server error")
        newsRepository.setFetchResult(Result.failure(error))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.ServerError>(result.exceptionOrNull())
    }

    @Test
    fun `invoke passes forceRefresh parameter to repository`() = runTest {
        val articles = createTestArticles(1)
        newsRepository.setFetchResult(Result.success(articles))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        useCase("technology", forceRefresh = true)

        assertEquals(true, newsRepository.lastFetchForceRefresh)
    }

    @Test
    fun `invoke uses default forceRefresh false when not specified`() = runTest {
        val articles = createTestArticles(1)
        newsRepository.setFetchResult(Result.success(articles))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        useCase("technology")

        assertEquals(false, newsRepository.lastFetchForceRefresh)
    }
}
