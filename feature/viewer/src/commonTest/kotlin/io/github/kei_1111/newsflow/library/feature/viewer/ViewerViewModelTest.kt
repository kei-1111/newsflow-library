package io.github.kei_1111.newsflow.library.feature.viewer

import app.cash.turbine.test
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticle
import io.github.kei_1111.newsflow.library.core.test.usecase.FakeGetArticleByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import platform.GameplayKit.GKState.Companion.state
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ViewerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getArticleByIdUseCase: FakeGetArticleByIdUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getArticleByIdUseCase = FakeGetArticleByIdUseCase()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization fetches article successfully`() = runTest {
        val article = createTestArticle(1)
        getArticleByIdUseCase.setResult(Result.success(article))
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
        )

        viewModel.uiState.test {
            skipItems(1) // LoadingはスキップされてしまうためskipItem(1)

            val state = awaitItem()
            assertIs<ViewerUiState.Stable>(state)
            assertEquals(article, state.viewingArticle)
        }
    }

    @Test
    fun `initialization fails to get article and transitions to error state`() = runTest {
        val error = NewsflowError.InternalError.ArticleNotFound("Article Not Found")
        getArticleByIdUseCase.setResult(Result.failure(error))
        val viewModel = ViewerViewModel(
            articleId = "valid-id",
            getArticleByIdUseCase = getArticleByIdUseCase,
        )

        viewModel.uiState.test {
            skipItems(1) // LoadingはスキップされてしまうためskipItem(1)

            val state = awaitItem()
            assertIs<ViewerUiState.Error>(state)
            assertEquals(error, state.error)
        }
    }

    @Test
    fun `initialization with blank articleId transitions to error state immediately`() = runTest {
        val viewModel = ViewerViewModel(
            articleId = "",
            getArticleByIdUseCase = getArticleByIdUseCase,
        )

        viewModel.uiState.test {
            skipItems(1) // StatefulBaseViewModel.uiState.stateIn

            val errorState = awaitItem()
            assertIs<ViewerUiState.Error>(errorState)
            assertIs<NewsflowError.InternalError.InvalidParameter>(errorState.error)
        }

        assertEquals(0, getArticleByIdUseCase.invocationCount)
    }

    @Test
    fun `initialization with whitespace only articleId transitions to error state immediately`() = runTest {
        val viewModel = ViewerViewModel(
            articleId = "   ",
            getArticleByIdUseCase = getArticleByIdUseCase,
        )

        viewModel.uiState.test {
            skipItems(1) // StatefulBaseViewModel.uiState.stateIn

            val errorState = awaitItem()
            assertIs<ViewerUiState.Error>(errorState)
            assertIs<NewsflowError.InternalError.InvalidParameter>(errorState.error)
        }

        assertEquals(0, getArticleByIdUseCase.invocationCount)
    }

    @Test
    fun `onClickBackButton emits NavigateBack effect`() = runTest {
        val article = createTestArticle(1)
        getArticleByIdUseCase.setResult(Result.success(article))
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
        )

        viewModel.uiEffect.test {
            viewModel.onUiAction(ViewerUiAction.OnClickBackButton)

            val effect = awaitItem()
            assertIs<ViewerUiEffect.NavigateBack>(effect)
        }
    }

    @Test
    fun `onClickShareButton emits ShareArticle effect with correct title and url`() = runTest {
        val article = createTestArticle(1)
        getArticleByIdUseCase.setResult(Result.success(article))
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
        )

        viewModel.uiEffect.test {
            viewModel.onUiAction(ViewerUiAction.OnClickShareButton(article))

            val effect = awaitItem()
            assertIs<ViewerUiEffect.ShareArticle>(effect)
            assertEquals(article.title, effect.title)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `article fetch passes correct articleId to use case`() = runTest {
        val articleId = "test-article-123"
        val article = createTestArticle(1)
        getArticleByIdUseCase.setResult(Result.success(article))
        val viewModel = ViewerViewModel(
            articleId = articleId,
            getArticleByIdUseCase = getArticleByIdUseCase,
        )

        viewModel.uiState.test {
            skipItems(2) // init{}が終わるまでスキップ
        }

        assertEquals(articleId, getArticleByIdUseCase.lastInvokedId)
        assertEquals(1, getArticleByIdUseCase.invocationCount)
    }
}
