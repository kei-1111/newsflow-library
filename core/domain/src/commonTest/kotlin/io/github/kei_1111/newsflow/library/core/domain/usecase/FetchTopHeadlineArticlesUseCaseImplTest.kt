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
        newsRepository.setResult(Result.success(articles))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isSuccess)
        assertEquals(articles, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val error = NewsflowError.NetworkFailure("Network error")
        newsRepository.setResult(Result.failure(error))
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
        newsRepository.setResult(Result.failure(error))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.Unauthorized>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates RateLimitExceeded error from repository`() = runTest {
        val error = NewsflowError.RateLimitExceeded("Rate limit exceeded")
        newsRepository.setResult(Result.failure(error))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.RateLimitExceeded>(result.exceptionOrNull())
    }

    @Test
    fun `invoke propagates ServerError from repository`() = runTest {
        val error = NewsflowError.ServerError("Internal server error")
        newsRepository.setResult(Result.failure(error))
        val useCase = FetchTopHeadlineArticlesUseCaseImpl(newsRepository)

        val result = useCase("technology")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.ServerError>(result.exceptionOrNull())
    }
}