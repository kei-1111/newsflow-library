package io.github.kei_1111.newsflow.library.feature.home

import app.cash.turbine.test
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCase
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fetchArticlesUseCase: FakeFetchArticlesUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fetchArticlesUseCase = FakeFetchArticlesUseCase()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onClickArticleCard sends NavigateViewer effect`() = runTest {
        fetchArticlesUseCase.setResult(Result.success(emptyList()))
        val viewModel = HomeViewModel(fetchArticlesUseCase)
        val article = createTestArticle(1)

        viewModel.uiEffect.test {
            viewModel.onUiAction(HomeUiAction.OnClickArticleCard(article))

            val effect = awaitItem()
            assertIs<HomeUiEffect.NavigateViewer>(effect)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `onSwipNewsCategoryPage calls changeNewsCategory and Success`() = runTest {
        val articles = createTestArticles(3)
        val category = NewsCategory.TECHNOLOGY
        fetchArticlesUseCase.setResult(Result.success(articles))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // createUiState + init -> fetch -> setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init -> fetch -> UseCase
            skipItems(1) // init -> fetch -> handleSuccess

            viewModel.onUiAction(HomeUiAction.OnSwipNewsCategoryPage(category))

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeUiState.Stable>(successState)
            assertEquals(category, successState.currentNewsCategory) // changeNewsCategory was called
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `onSwipNewsCategoryPage calls changeNewsCategory and Error`() = runTest {
        val error = NewsflowError.NetworkFailure("Network Error")
        val category = NewsCategory.TECHNOLOGY
        fetchArticlesUseCase.setResult(Result.failure(error))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // createUiState + init -> fetch -> setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init -> fetch -> UseCase
            skipItems(1) // init -> fetch -> handleSuccess

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
    fun `onClickNewsCategoryTag calls changeNewsCategory and Success`() = runTest {
        val articles = createTestArticles(3)
        val category = NewsCategory.BUSINESS
        fetchArticlesUseCase.setResult(Result.success(articles))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // createUiState + init -> fetch -> setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init -> fetch -> UseCase
            skipItems(1) // init -> fetch -> handleSuccess

            viewModel.onUiAction(HomeUiAction.OnClickNewsCategoryTag(category))

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeUiState.Stable>(successState)
            assertEquals(category, successState.currentNewsCategory) // changeNewsCategory was called
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `onClickNewsCategoryTag calls changeNewsCategory and Error`() = runTest {
        val error = NewsflowError.NetworkFailure("Network Error")
        val category = NewsCategory.BUSINESS
        fetchArticlesUseCase.setResult(Result.failure(error))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // createUiState + init -> fetch -> setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init -> fetch -> UseCase
            skipItems(1) // init -> fetch -> handleSuccess

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
    fun `onClickRetryButton calls fetchArticles and Success`() = runTest {
        val articles = createTestArticles(3)
        val category = NewsCategory.GENERAL // default Category
        fetchArticlesUseCase.setResult(Result.success(articles))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // createUiState + init -> fetch -> setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init -> fetch -> UseCase
            skipItems(1) // init -> fetch -> handleSuccess

            viewModel.onUiAction(HomeUiAction.OnClickRetryButton)

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertIs<HomeUiState.Stable>(successState)
            assertEquals(category, successState.currentNewsCategory)
            assertEquals(articles, successState.articlesByCategory[category])
        }
    }

    @Test
    fun `onClickRetryButton calls fetchArticles and Error`() = runTest {
        val error = NewsflowError.NetworkFailure("Network error")
        fetchArticlesUseCase.setResult(Result.failure(error))
        val viewModel = HomeViewModel(fetchArticlesUseCase)

        viewModel.uiState.test {
            skipItems(2) // createUiState + init -> fetch -> setLoading
            testDispatcher.scheduler.advanceUntilIdle() // init -> fetch -> UseCase
            skipItems(1) // init -> fetch -> handleSuccess

            viewModel.onUiAction(HomeUiAction.OnClickRetryButton)

            val loadingState = awaitItem()
            assertIs<HomeUiState.Stable>(loadingState)
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertIs<HomeUiState.Error>(errorState)
            assertEquals(error, errorState.error)
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

    private class FakeFetchArticlesUseCase : FetchArticlesUseCase {
        private var result: Result<List<Article>> = Result.success(emptyList())
        var invocationCount = 0
            private set
        var lastCategory: String = ""
            private set

        fun setResult(result: Result<List<Article>>) {
            this.result = result
        }

        override suspend fun invoke(category: String): Result<List<Article>> {
            invocationCount++
            lastCategory = category
            return result
        }
    }
}
