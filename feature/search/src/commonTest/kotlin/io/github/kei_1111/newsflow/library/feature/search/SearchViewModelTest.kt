package io.github.kei_1111.newsflow.library.feature.search

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.github.kei_1111.newsflow.library.core.domain.usecase.SearchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Stable with empty query and articles`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.state.test {
            val initialState = awaitItem()
            assertIs<SearchState.Stable>(initialState)
            assertEquals("", initialState.query)
            assertEquals(emptyList(), initialState.articles)
            assertFalse(initialState.isSearching)
            assertNull(initialState.selectedArticle)
        }
    }

    @Test
    fun `UpdateQuery intent updates query in state`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        everySuspend { searchArticlesUseCase(any()) } returns Result.success(emptyList())
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.UpdateQuery("kotlin"))

            val updatedState = awaitItem()
            assertIs<SearchState.Stable>(updatedState)
            assertEquals("kotlin", updatedState.query)
        }
    }

    @Test
    fun `debounce search executes after delay`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val articles = createTestArticles(3)
        everySuspend { searchArticlesUseCase(any()) } returns Result.success(articles)
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.UpdateQuery("kotlin"))
            val queryUpdated = awaitItem()
            assertIs<SearchState.Stable>(queryUpdated)
            assertEquals("kotlin", queryUpdated.query)

            // デバウンス時間(1000ms)経過前は検索されない
            advanceTimeBy(500)
            expectNoEvents()

            // デバウンス時間経過後に検索が実行される
            advanceTimeBy(600)
            testDispatcher.scheduler.advanceUntilIdle()

            val searchingState = awaitItem()
            assertIs<SearchState.Stable>(searchingState)
            assertTrue(searchingState.isSearching)

            val successState = awaitItem()
            assertIs<SearchState.Stable>(successState)
            assertFalse(successState.isSearching)
            assertEquals(articles, successState.articles)
        }
    }

    @Test
    fun `debounce resets when new query is entered`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val articles = createTestArticles(3)
        everySuspend { searchArticlesUseCase(any()) } returns Result.success(articles)
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.UpdateQuery("kot"))
            awaitItem() // query updated

            advanceTimeBy(500)

            viewModel.onIntent(SearchIntent.UpdateQuery("kotlin"))
            awaitItem() // query updated again

            // 最初のクエリから1000ms経過しても、2回目のクエリからはまだ500msなので検索されない
            advanceTimeBy(500)
            expectNoEvents()

            // 2回目のクエリから1000ms経過後に検索が実行される
            advanceTimeBy(600)
            testDispatcher.scheduler.advanceUntilIdle()

            val searchingState = awaitItem()
            assertIs<SearchState.Stable>(searchingState)

            val successState = awaitItem()
            assertIs<SearchState.Stable>(successState)
            assertEquals(articles, successState.articles)
        }
    }

    @Test
    fun `search failure transitions to error state`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network Error")
        everySuspend { searchArticlesUseCase(any()) } returns Result.failure(error)
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.UpdateQuery("kotlin"))
            awaitItem() // query updated

            // デバウンス時間経過後に検索が実行される
            advanceTimeBy(1100)
            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = expectMostRecentItem()
            assertIs<SearchState.Error>(errorState)
            assertEquals("kotlin", errorState.query)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `ClearQuery intent clears query and articles`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        everySuspend { searchArticlesUseCase(any()) } returns Result.success(emptyList())
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.UpdateQuery("kotlin"))
            val updatedState = awaitItem()
            assertIs<SearchState.Stable>(updatedState)
            assertEquals("kotlin", updatedState.query)

            viewModel.onIntent(SearchIntent.ClearQuery)
            testDispatcher.scheduler.advanceUntilIdle()

            val clearedState = expectMostRecentItem()
            assertIs<SearchState.Stable>(clearedState)
            assertEquals("", clearedState.query)
            assertEquals(emptyList(), clearedState.articles)
        }
    }

    @Test
    fun `RetrySearch intent retries search with current query`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val articles = createTestArticles(3)
        everySuspend { searchArticlesUseCase(any()) } returns Result.success(articles)
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.UpdateQuery("kotlin"))
            awaitItem() // query updated

            viewModel.onIntent(SearchIntent.RetrySearch)
            testDispatcher.scheduler.advanceUntilIdle()

            val successState = expectMostRecentItem()
            assertIs<SearchState.Stable>(successState)
            assertEquals(articles, successState.articles)
        }
    }

    @Test
    fun `SelectArticle intent emits NavigateViewer effect`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.effect.test {
            viewModel.onIntent(SearchIntent.SelectArticle(article))

            val effect = awaitItem()
            assertIs<SearchEffect.NavigateViewer>(effect)
            assertEquals(article.id, effect.articleId)
        }
    }

    @Test
    fun `ShowArticleOverview intent updates selectedArticle`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.ShowArticleOverview(article))

            val updatedState = awaitItem()
            assertIs<SearchState.Stable>(updatedState)
            assertEquals(article, updatedState.selectedArticle)
        }
    }

    @Test
    fun `DismissArticleOverview intent clears selectedArticle`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onIntent(SearchIntent.ShowArticleOverview(article))
            val selectedState = awaitItem()
            assertIs<SearchState.Stable>(selectedState)
            assertEquals(article, selectedState.selectedArticle)

            viewModel.onIntent(SearchIntent.DismissArticleOverview)

            val clearedState = awaitItem()
            assertIs<SearchState.Stable>(clearedState)
            assertNull(clearedState.selectedArticle)
        }
    }

    @Test
    fun `CopyArticleUrl intent emits CopyUrl effect when article is selected`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.onIntent(SearchIntent.ShowArticleOverview(article))

        viewModel.effect.test {
            viewModel.onIntent(SearchIntent.CopyArticleUrl)

            val effect = awaitItem()
            assertIs<SearchEffect.CopyUrl>(effect)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `CopyArticleUrl intent does nothing when no article is selected`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.effect.test {
            viewModel.onIntent(SearchIntent.CopyArticleUrl)

            expectNoEvents()
        }
    }

    @Test
    fun `ShareArticle intent emits ShareArticle effect when article is selected`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.onIntent(SearchIntent.ShowArticleOverview(article))

        viewModel.effect.test {
            viewModel.onIntent(SearchIntent.ShareArticle)

            val effect = awaitItem()
            assertIs<SearchEffect.ShareArticle>(effect)
            assertEquals(article.title, effect.title)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `ShareArticle intent does nothing when no article is selected`() = runTest {
        val searchArticlesUseCase = mock<SearchArticlesUseCase>()
        val viewModel = SearchViewModel(searchArticlesUseCase)

        viewModel.effect.test {
            viewModel.onIntent(SearchIntent.ShareArticle)

            expectNoEvents()
        }
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