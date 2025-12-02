package io.github.kei_1111.newsflow.library.core.domain.usecase

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetArticleByIdUseCaseImplTest {

    @Test
    fun `invoke returns article when repository returns article`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val article = createTestArticle(index = 1)
        everySuspend { newsRepository.getArticleById(any()) } returns Result.success(article)
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        val result = useCase(article.id)

        assertTrue(result.isSuccess)
        assertEquals(article, result.getOrNull())
        verifySuspend { newsRepository.getArticleById(article.id) }
    }

    @Test
    fun `invoke returns failure when repository returns failure`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.InternalError.ArticleNotFound("Article not found")
        everySuspend { newsRepository.getArticleById(any()) } returns Result.failure(error)
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        val result = useCase("nonexistent-id")

        assertTrue(result.isFailure)
        assertIs<NewsflowError.InternalError.ArticleNotFound>(result.exceptionOrNull())
        verifySuspend { newsRepository.getArticleById("nonexistent-id") }
    }

    @Test
    fun `invoke propagates errors from repository`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network error")
        everySuspend { newsRepository.getArticleById(any()) } returns Result.failure(error)
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        val result = useCase("test-id")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NewsflowError.NetworkError.NetworkFailure>(exception)
        assertEquals("Network error", exception.message)
    }

    @Test
    fun `invoke calls repository with correct article id`() = runTest {
        val newsRepository = mock<NewsRepository>()
        val article = createTestArticle(index = 1)
        everySuspend { newsRepository.getArticleById(any()) } returns Result.success(article)
        val useCase = GetArticleByIdUseCaseImpl(newsRepository)

        useCase("specific-article-id")

        verifySuspend(exactly(1)) { newsRepository.getArticleById("specific-article-id") }
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
}
