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

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle() // init->fetch->UseCase

            val successState = expectMostRecentItem()
            assertIs<HomeUiState.Stable>(successState)
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

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle() // 全ての状態遷移を完了させる

            val errorState = expectMostRecentItem()
            assertIs<HomeUiState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `onClickArticleCard emits NavigateViewer effect with article url`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.uiEffect.test {
            viewModel.onUiAction(HomeUiAction.OnClickArticleCard(article))

            val effect = awaitItem()
            assertIs<HomeUiEffect.NavigateViewer>(effect)
            assertEquals(article.id, effect.id)
        }
    }

    @Test
    fun `onSwipeNewsCategoryPage changes category and fetches articles successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val articles = createTestArticles(3)
        val category = NewsCategory.TECHNOLOGY
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(articles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

            viewModel.onUiAction(HomeUiAction.OnSwipNewsCategoryPage(category))

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeUiState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(category, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `onSwipeNewsCategoryPage changes category but fails to fetch articles`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network Error")
        val category = NewsCategory.TECHNOLOGY
        // 初期化は成功させる
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

            // アクション実行時にエラーを返すように設定
            everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
            viewModel.onUiAction(HomeUiAction.OnSwipNewsCategoryPage(category))

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertIs<HomeUiState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `onClickNewsCategoryTag changes category and fetches articles successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val articles = createTestArticles(3)
        val category = NewsCategory.BUSINESS
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(articles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

            viewModel.onUiAction(HomeUiAction.OnClickNewsCategoryTag(category))

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeUiState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(category, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `onClickNewsCategoryTag changes category but fails to fetch articles`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network Error")
        val category = NewsCategory.BUSINESS
        // 初期化は成功させる
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

            // アクション実行時にエラーを返すように設定
            everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
            viewModel.onUiAction(HomeUiAction.OnClickNewsCategoryTag(category))

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertIs<HomeUiState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `onClickRetryButton refetches current category articles successfully`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val articles = createTestArticles(3)
        val category = NewsCategory.GENERAL // default Category
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(articles)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

            viewModel.onUiAction(HomeUiAction.OnClickRetryButton)

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeUiState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(category, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `onClickRetryButton refetches articles but fails`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        val error = NewsflowError.NetworkError.NetworkFailure("Network error")
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.failure(error)
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

            viewModel.onUiAction(HomeUiAction.OnClickRetryButton)

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertIs<HomeUiState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `onClickMoreBottom updates selectedArticle in state`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.uiState.test {
            skipInitialization()

            viewModel.onUiAction(HomeUiAction.OnClickMoreBottom(article))

            val updatedState = awaitItem()
            assertIs<HomeUiState.Stable>(updatedState)
            assertEquals(article, updatedState.selectedArticle)
        }
    }

    @Test
    fun `onDismissArticleOverviewBottomSheet clears selectedArticle`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.uiState.test {
            skipInitialization()

            // まず記事を選択
            viewModel.onUiAction(HomeUiAction.OnClickMoreBottom(article))
            val selectedState = awaitItem()
            assertIs<HomeUiState.Stable>(selectedState)
            assertEquals(article, selectedState.selectedArticle)

            // ボトムシートを閉じる
            viewModel.onUiAction(HomeUiAction.OnDismissArticleOverviewBottomSheet)
            val clearedState = awaitItem()
            assertIs<HomeUiState.Stable>(clearedState)
            assertEquals(null, clearedState.selectedArticle)
        }
    }

    @Test
    fun `onClickCopyUrlButton emits CopyUrl effect when article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.onUiAction(HomeUiAction.OnClickMoreBottom(article))

        viewModel.uiEffect.test {
            viewModel.onUiAction(HomeUiAction.OnClickCopyUrlButton)

            val effect = awaitItem()
            assertIs<HomeUiEffect.CopyUrl>(effect)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `onClickCopyUrlButton does nothing when no article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiEffect.test {
            viewModel.onUiAction(HomeUiAction.OnClickCopyUrlButton)

            expectNoEvents()
        }
    }

    @Test
    fun `onClickShareButton emits ShareArticle effect when article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        // 先に記事を選択
        viewModel.onUiAction(HomeUiAction.OnClickMoreBottom(article))

        viewModel.uiEffect.test {
            viewModel.onUiAction(HomeUiAction.OnClickShareButton)

            val effect = awaitItem()
            assertIs<HomeUiEffect.ShareArticle>(effect)
            assertEquals(article.title, effect.title)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `onClickShareButton does nothing when no article is selected`() = runTest {
        val fetchArticlesUseCase = mock<FetchTopHeadlineArticlesUseCase>()
        everySuspend { fetchArticlesUseCase(any(), any()) } returns Result.success(emptyList())
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiEffect.test {
            viewModel.onUiAction(HomeUiAction.OnClickShareButton)

            expectNoEvents()
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.skipInitialization() {
        skipItems(2) // StatefulBaseViewModel.uiState.stateIn + init->fetch->setLoading
        testDispatcher.scheduler.advanceUntilIdle() // init->fetch->UseCase
        skipItems(1) // init->fetch->handleXXXX
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
