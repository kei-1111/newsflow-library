package io.github.kei_1111.newsflow.library.feature.viewer

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.github.kei_1111.newsflow.library.core.domain.usecase.GetArticleByIdUseCase
import io.github.kei_1111.newsflow.library.core.domain.usecase.SummarizeArticleUseCase
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ViewerViewModelTest {

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
    fun `initialization fetches article successfully`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // LoadingはスキップされてしまうためskipItem(1)

            val state = awaitItem()
            assertIs<ViewerState.Stable>(state)
            assertEquals(article, state.viewingArticle)
        }
    }

    @Test
    fun `initialization fails to get article and transitions to error state`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val error = NewsflowError.InternalError.ArticleNotFound("Article Not Found")
        everySuspend { getArticleByIdUseCase(any()) } returns Result.failure(error)
        val viewModel = ViewerViewModel(
            articleId = "valid-id",
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // LoadingはスキップされてしまうためskipItem(1)

            val state = awaitItem()
            assertIs<ViewerState.Error>(state)
            assertEquals(error, state.error)
        }
    }

    @Test
    fun `initialization with blank articleId transitions to error state immediately`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = "",
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // StatefulBaseViewModel.state.stateIn

            val errorState = awaitItem()
            assertIs<ViewerState.Error>(errorState)
            assertIs<NewsflowError.InternalError.InvalidParameter>(errorState.error)
        }

        verifySuspend(exactly(0)) { getArticleByIdUseCase(any()) }
    }

    @Test
    fun `initialization with whitespace only articleId transitions to error state immediately`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = "   ",
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // StatefulBaseViewModel.state.stateIn

            val errorState = awaitItem()
            assertIs<ViewerState.Error>(errorState)
            assertIs<NewsflowError.InternalError.InvalidParameter>(errorState.error)
        }

        verifySuspend(exactly(0)) { getArticleByIdUseCase(any()) }
    }

    @Test
    fun `NavigateBack intent emits NavigateBack effect`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.effect.test {
            viewModel.onIntent(ViewerIntent.NavigateBack)

            val effect = awaitItem()
            assertIs<ViewerEffect.NavigateBack>(effect)
        }
    }

    @Test
    fun `ShareArticle intent emits ShareArticle effect with correct title and url`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        // 記事の取得が完了するまで待機
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(ViewerIntent.ShareArticle)

            val effect = awaitItem()
            assertIs<ViewerEffect.ShareArticle>(effect)
            assertEquals(article.title, effect.title)
            assertEquals(article.url, effect.url)
        }
    }

    @Test
    fun `article fetch passes correct articleId to use case`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val articleId = "test-article-123"
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = articleId,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(2) // init{}が終わるまでスキップ
        }

        verifySuspend(exactly(1)) { getArticleByIdUseCase(articleId) }
    }

    @Test
    fun `StartWebViewLoading intent sets isWebViewLoading to true`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // 初期状態をスキップ

            // 初期状態はisWebViewLoading = true
            val initialState = awaitItem()
            assertIs<ViewerState.Stable>(initialState)
            assertEquals(true, initialState.isWebViewLoading)

            // FinishWebViewLoadingでfalseにしてからStartWebViewLoadingをテスト
            viewModel.onIntent(ViewerIntent.FinishWebViewLoading)
            val finishedState = awaitItem()
            assertIs<ViewerState.Stable>(finishedState)
            assertEquals(false, finishedState.isWebViewLoading)

            // StartWebViewLoadingでtrueに戻る
            viewModel.onIntent(ViewerIntent.StartWebViewLoading)
            val startedState = awaitItem()
            assertIs<ViewerState.Stable>(startedState)
            assertEquals(true, startedState.isWebViewLoading)
        }
    }

    @Test
    fun `FinishWebViewLoading intent sets isWebViewLoading to false`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // 初期状態をスキップ

            // 初期状態はisWebViewLoading = true
            val initialState = awaitItem()
            assertIs<ViewerState.Stable>(initialState)
            assertEquals(true, initialState.isWebViewLoading)

            // FinishWebViewLoadingでfalseに変更
            viewModel.onIntent(ViewerIntent.FinishWebViewLoading)
            val finishedState = awaitItem()
            assertIs<ViewerState.Stable>(finishedState)
            assertEquals(false, finishedState.isWebViewLoading)
        }
    }

    // Summarize tests
    @Test
    fun `SummarizeArticle intent starts summarization and updates state`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        every { summarizeArticleUseCase(any()) } returns flowOf("Summary text")
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // Loading
            awaitItem() // Stable with article

            viewModel.onIntent(ViewerIntent.SummarizeArticle)
            testDispatcher.scheduler.advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertIs<ViewerState.Stable>(finalState)
            assertEquals("Summary text", finalState.summary)
            assertFalse(finalState.isSummarizing)
        }
    }

    @Test
    fun `SummarizeArticle intent emits SummaryError effect on failure`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        val error = NewsflowError.AIError.GenerationFailed("Generation failed")
        every { summarizeArticleUseCase(any()) } returns flow { throw error }
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(ViewerIntent.SummarizeArticle)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertIs<ViewerEffect.SummaryError>(effect)
            assertIs<NewsflowError.AIError.GenerationFailed>(effect.error)
        }
    }

    @Test
    fun `DismissSummary intent clears summary state`() = runTest {
        val getArticleByIdUseCase = mock<GetArticleByIdUseCase>()
        val article = createTestArticle(1)
        everySuspend { getArticleByIdUseCase(any()) } returns Result.success(article)
        val summarizeArticleUseCase = mock<SummarizeArticleUseCase>()
        every { summarizeArticleUseCase(any()) } returns flowOf("Summary text")
        val viewModel = ViewerViewModel(
            articleId = article.id,
            getArticleByIdUseCase = getArticleByIdUseCase,
            summarizeArticleUseCase = summarizeArticleUseCase,
        )

        viewModel.state.test {
            skipItems(1) // Loading
            awaitItem() // Stable with article

            viewModel.onIntent(ViewerIntent.SummarizeArticle)
            testDispatcher.scheduler.advanceUntilIdle()

            val withSummary = expectMostRecentItem()
            assertIs<ViewerState.Stable>(withSummary)
            assertEquals("Summary text", withSummary.summary)

            viewModel.onIntent(ViewerIntent.DismissSummary)
            val clearedState = awaitItem()
            assertIs<ViewerState.Stable>(clearedState)
            assertEquals("", clearedState.summary)
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
}
