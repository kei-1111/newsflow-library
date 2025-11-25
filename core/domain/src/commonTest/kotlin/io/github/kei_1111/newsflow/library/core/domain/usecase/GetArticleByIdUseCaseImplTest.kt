package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticle
import io.github.kei_1111.newsflow.library.core.test.repository.FakeNewsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetArticleByIdUseCaseImplTest {

    private lateinit var newsRepository: FakeNewsRepository

    @BeforeTest
    fun setup() {
        newsRepository = FakeNewsRepository()
    }

    @Test
    fun `invoke returns article when repository returns article`() = runTest {
        val article = createTestArticle(index = 1)
        newsRepository.setGetByIdResult(Result.success(article))
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        val result = useCase(article.id)

        assertTrue(result.isSuccess)
        assertEquals(article, result.getOrNull())
        assertEquals(article.id, newsRepository.lastGetByIdArticleId)
    }

    @Test
    fun `invoke returns null when repository returns null`() = runTest {
        newsRepository.setGetByIdResult(Result.success(null))
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        val result = useCase("nonexistent-id")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        assertEquals("nonexistent-id", newsRepository.lastGetByIdArticleId)
    }

    @Test
    fun `invoke propagates errors from repository`() = runTest {
        val error = NewsflowError.NetworkFailure("Network error")
        newsRepository.setGetByIdResult(Result.failure(error))
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        val result = useCase("test-id")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkFailure>(exception)
        assertEquals("Network error", exception.message)
    }

    @Test
    fun `invoke calls repository with correct article id`() = runTest {
        newsRepository.setGetByIdResult(Result.success(null))
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        useCase("specific-article-id")

        assertEquals("specific-article-id", newsRepository.lastGetByIdArticleId)
        assertEquals(1, newsRepository.getByIdInvocationCount)
    }
}
