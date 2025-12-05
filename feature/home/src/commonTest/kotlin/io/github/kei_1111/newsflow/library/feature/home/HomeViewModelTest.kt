package io.github.kei_1111.newsflow.library.feature.home

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCase
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

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
    fun `initialization fetches GENERAL category articles successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val articles = createTestArticles(3)
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(articles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            testDispatcher.scheduler.advanceUntilIdle() // init->fetch->UseCase

            val successState = expectMostRecentItem()
            assertIs<HomeState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(NewsCategory.GENERAL, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[NewsCategory.GENERAL])
        }
    }

    @Test
    fun `initialization fails to fetch articles and transitions to error state`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network Error")
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            testDispatcher.scheduler.advanceUntilIdle() // 全ての状態遷移を完了させる

            val errorState = expectMostRecentItem()
            assertIs<HomeState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `NavigateViewer intent emits NavigateViewer effect with article id`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.NavigateViewer(article))

            val effect = awaitItem()
            assertIs<HomeEffect.NavigateViewer>(effect)
            assertEquals(article.id, effect.id)
        }
    }

    @Test
    fun `ChangeCategory intent via swipe changes category and fetches articles successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val articles = createTestArticles(3)
        val category = NewsCategory.TECHNOLOGY
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(articles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            viewModel.onIntent(HomeIntent.ChangeCategory(category))

            val loadingState = awaitItem()
            assertIs<HomeState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(category, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `ChangeCategory intent via swipe changes category but fails to fetch articles`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network Error")
        val category = NewsCategory.TECHNOLOGY
        // 初期化は成功させる
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            // アクション実行時にエラーを返すように設定
            everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
            viewModel.onIntent(HomeIntent.ChangeCategory(category))

            val loadingState = awaitItem()
            assertIs<HomeState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertIs<HomeState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `ChangeCategory intent via tag click changes category and fetches articles successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val articles = createTestArticles(3)
        val category = NewsCategory.BUSINESS
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(articles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            viewModel.onIntent(HomeIntent.ChangeCategory(category))

            val loadingState = awaitItem()
            assertIs<HomeState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(category, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `ChangeCategory intent via tag click changes category but fails to fetch articles`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network Error")
        val category = NewsCategory.BUSINESS
        // 初期化は成功させる
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            // アクション実行時にエラーを返すように設定
            everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
            viewModel.onIntent(HomeIntent.ChangeCategory(category))

            val loadingState = awaitItem()
            assertIs<HomeState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertIs<HomeState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `RetryLoad intent refetches current category articles successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val articles = createTestArticles(3)
        val category = NewsCategory.GENERAL // default Category
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(articles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            viewModel.onIntent(HomeIntent.RetryLoad)

            val loadingState = awaitItem()
            assertIs<HomeState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(category, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `RetryLoad intent refetches articles but fails`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network error")
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            viewModel.onIntent(HomeIntent.RetryLoad)

            val loadingState = awaitItem()
            assertIs<HomeState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertIs<HomeState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `ShowArticleOverview intent updates selectedArticle in state`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.state.test {
            skipInitialization()

            viewModel.onIntent(HomeIntent.ShowArticleOverview(article))

            val updatedState = awaitItem()
            assertIs<HomeState.Stable>(updatedState)
            assertEquals(article, updatedState.selectedArticle)
        }
    }

    @Test
    fun `DismissArticleOverview intent clears selectedArticle`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.state.test {
            skipInitialization()

            // まず記事を選択
            viewModel.onIntent(HomeIntent.ShowArticleOverview(article))
            val selectedState = awaitItem()
            assertIs<HomeState.Stable>(selectedState)
            assertEquals(article, selectedState.selectedArticle)

            // ボトムシートを閉じる
            viewModel.onIntent(HomeIntent.DismissArticleOverview)
            val clearedState = awaitItem()
            assertIs<HomeState.Stable>(clearedState)
            assertEquals(null, clearedState.selectedArticle)
        }
    }

    @Test
    fun `CopyArticleUrl intent emits CopyUrl effect when article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.onIntent(HomeIntent.ShowArticleOverview(article))

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.CopyArticleUrl)

            val effect = awaitItem()
            assertIs<HomeEffect.CopyUrl>(effect)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `CopyArticleUrl intent does nothing when no article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.CopyArticleUrl)

            expectNoEvents()
        }
    }

    @Test
    fun `ShareArticle intent emits ShareArticle effect when article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        // 先に記事を選択
        viewModel.onIntent(HomeIntent.ShowArticleOverview(article))

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.ShareArticle)

            val effect = awaitItem()
            assertIs<HomeEffect.ShareArticle>(effect)
            assertEquals(article.title, effect.title)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `ShareArticle intent does nothing when no article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.ShareArticle)

            expectNoEvents()
        }
    }

    @Test
    fun `RefreshArticles intent sets isRefreshing and fetches articles with forceRefresh successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val initialArticles = createTestArticles(3, "Initial")
        val refreshedArticles = createTestArticles(3, "Refreshed")
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(initialArticles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(refreshedArticles)
            viewModel.onIntent(HomeIntent.RefreshArticles)

            val refreshingState = awaitItem()
            assertIs<HomeState.Stable>(refreshingState)
            assertTrue(refreshingState.isRefreshing)

            val successState = awaitItem()
            assertIs<HomeState.Stable>(successState)
            assertFalse(successState.isRefreshing)
            assertEquals(refreshedArticles, successState.articlesByCategory[NewsCategory.GENERAL])
        }
    }

    @Test
    fun `RefreshArticles intent fails and sets error state`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val initialArticles = createTestArticles(3, "Initial")
        val error = NewsflowError.NetworkError.NetworkFailure("Network Error")
        // 初期化は成功させる
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(initialArticles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.state.test {
            skipInitialization()

            // リフレッシュ時にエラーを返すように設定
            everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
            viewModel.onIntent(HomeIntent.RefreshArticles)

            val refreshingState = awaitItem()
            assertIs<HomeState.Stable>(refreshingState)
            assertTrue(refreshingState.isRefreshing)

            val errorState = awaitItem()
            assertIs<HomeState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.skipInitialization() {
        // StatefulBaseViewModel.state.stateIn + init->fetch->setLoading (値が切り替わるのが早く2回値が変わるがskipItem.countは1でいい)
        skipItems(1)
        // init->fetch->UseCase
        testDispatcher.scheduler.advanceUntilIdle()
        // init->fetch->handleXXXX
        skipItems(1)
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
