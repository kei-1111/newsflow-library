package io.github.kei_1111.newsflow.library.feature.home

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticle
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticles
import io.github.kei_1111.newsflow.library.core.test.usecase.FakeFetchTopHeadlineArticlesUseCase
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
    private lateinit var fetchArticlesUseCase: FakeFetchTopHeadlineArticlesUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fetchArticlesUseCase = FakeFetchTopHeadlineArticlesUseCase()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization fetches GENERAL category articles successfully`() = runTest {
        val articles = createTestArticles(3)
        fetchArticlesUseCase.setResult(Result.success(articles))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // StatefulBaseViewModel.uiState.stateIn + init->fetch->setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init->fetch->UseCase

            val successState = awaitItem()
            assertIs<HomeUiState.Stable>(successState)
            assertFalse(successState.isLoading)
            assertEquals(NewsCategory.GENERAL, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[NewsCategory.GENERAL])
        }
    }

    @Test
    fun `initialization fails to fetch articles and transitions to error state`() = runTest {
        val error = NewsflowError.NetworkFailure("Network Error")
        fetchArticlesUseCase.setResult(Result.failure(error))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // StatefulBaseViewModel.uiState.stateIn + init->fetch->setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init->fetch->UseCase

            val errorState = awaitItem()
            assertIs<HomeUiState.Error>(errorState)
            assertEquals(error, errorState.error)
        }
    }

    @Test
    fun `onClickArticleCard emits NavigateViewer effect with article url`() = runTest {
        fetchArticlesUseCase.setResult(Result.success(emptyList()))
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
        val articles = createTestArticles(3)
        val category = NewsCategory.TECHNOLOGY
        fetchArticlesUseCase.setResult(Result.success(articles))
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
        val error = NewsflowError.NetworkFailure("Network Error")
        val category = NewsCategory.TECHNOLOGY
        fetchArticlesUseCase.setResult(Result.failure(error))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

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
        val articles = createTestArticles(3)
        val category = NewsCategory.BUSINESS
        fetchArticlesUseCase.setResult(Result.success(articles))
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
        val error = NewsflowError.NetworkFailure("Network Error")
        val category = NewsCategory.BUSINESS
        fetchArticlesUseCase.setResult(Result.failure(error))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipInitialization()

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
        val articles = createTestArticles(3)
        val category = NewsCategory.GENERAL // default Category
        fetchArticlesUseCase.setResult(Result.success(articles))
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
        val error = NewsflowError.NetworkFailure("Network error")
        fetchArticlesUseCase.setResult(Result.failure(error))
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

    private suspend fun <T> ReceiveTurbine<T>.skipInitialization() {
        skipItems(2) // StatefulBaseViewModel.uiState.stateIn + init->fetch->setLoading
        testDispatcher.scheduler.advanceUntilIdle() // init->fetch->UseCase
        skipItems(1) // init->fetch->handleXXXX
    }
}
