package io.github.kei_1111.newsflow.library.core.domain.usecase

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import io.github.kei_1111.newsflow.library.core.data.repository.SummaryRepository
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummarizeArticleUseCaseImplTest {

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        every { summaryRepository.summarizeArticle("https://example.com/article") } returns
            flowOf("Summary ", "text")
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase("https://example.com/article").test {
            assertEquals("Summary ", awaitItem())
            assertEquals("text", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke passes articleUrl to repository`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        val testUrl = "https://example.com/test-article"
        every { summaryRepository.summarizeArticle(testUrl) } returns flowOf("Summary")
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase(testUrl).test {
            awaitItem()
            awaitComplete()
        }

        verify { summaryRepository.summarizeArticle(testUrl) }
    }

    @Test
    fun `invoke propagates Unauthorized error from repository`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        every { summaryRepository.summarizeArticle("https://example.com/article") } returns flow {
            throw NewsflowError.NetworkError.Unauthorized("Invalid API key")
        }
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.Unauthorized>(error)
            assertEquals("Invalid API key", error.message)
        }
    }

    @Test
    fun `invoke propagates RateLimitExceeded error from repository`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        every { summaryRepository.summarizeArticle("https://example.com/article") } returns flow {
            throw NewsflowError.NetworkError.RateLimitExceeded("Rate limit exceeded")
        }
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.RateLimitExceeded>(error)
            assertEquals("Rate limit exceeded", error.message)
        }
    }

    @Test
    fun `invoke propagates ContentFiltered error from repository`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        every { summaryRepository.summarizeArticle("https://example.com/article") } returns flow {
            throw NewsflowError.NetworkError.ContentFiltered("Content filtered")
        }
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.ContentFiltered>(error)
            assertEquals("Content filtered", error.message)
        }
    }

    @Test
    fun `invoke propagates ServerError error from repository`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        every { summaryRepository.summarizeArticle("https://example.com/article") } returns flow {
            throw NewsflowError.NetworkError.ServerError("Server error")
        }
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase("https://example.com/article").test {
            val error = awaitError()
            assertIs<NewsflowError.NetworkError.ServerError>(error)
            assertEquals("Server error", error.message)
        }
    }

    @Test
    fun `invoke emits multiple chunks in order`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        every { summaryRepository.summarizeArticle("https://example.com/article") } returns
            flowOf("First ", "Second ", "Third")
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase("https://example.com/article").test {
            assertEquals("First ", awaitItem())
            assertEquals("Second ", awaitItem())
            assertEquals("Third", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke handles empty flow from repository`() = runTest {
        val summaryRepository = mock<SummaryRepository>()
        every { summaryRepository.summarizeArticle("https://example.com/article") } returns
            flowOf()
        val useCase = SummarizeArticleUseCaseImpl(summaryRepository)

        useCase("https://example.com/article").test {
            awaitComplete()
        }
    }
}
