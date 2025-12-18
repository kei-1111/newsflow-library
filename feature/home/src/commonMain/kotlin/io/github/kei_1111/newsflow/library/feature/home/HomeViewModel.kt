package io.github.kei_1111.newsflow.library.feature.home

import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCase
import io.github.kei_1111.newsflow.library.core.domain.usecase.SummarizeArticleUseCase
import io.github.kei_1111.newsflow.library.core.logger.Logger
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class HomeViewModel(
    private val fetchTopHeadlineArticlesUseCase: FetchTopHeadlineArticlesUseCase,
    private val summarizeArticleUseCase: SummarizeArticleUseCase,
) : StatefulBaseViewModel<HomeViewModelState, HomeState, HomeIntent, HomeEffect>() {

    override fun createInitialViewModelState(): HomeViewModelState = HomeViewModelState()
    override fun createInitialState(): HomeState = HomeState.Stable()

    init {
        fetchArticles(_viewModelState.value.currentNewsCategory)
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.NavigateViewer -> {
                sendEffect(HomeEffect.NavigateViewer(intent.article.id))
            }
            is HomeIntent.ChangeCategory -> {
                changeNewsCategory(intent.newsCategory)
            }
            is HomeIntent.ShowArticleOverview -> {
                updateViewModelState { copy(selectedArticle = intent.article) }
            }
            is HomeIntent.DismissArticleOverview -> {
                updateViewModelState { copy(selectedArticle = null) }
            }
            is HomeIntent.CopyArticleUrl -> {
                _viewModelState.value.selectedArticle?.let {
                    sendEffect(HomeEffect.CopyUrl(it.url))
                }
            }
            is HomeIntent.ShareArticle -> {
                _viewModelState.value.selectedArticle?.let {
                    sendEffect(
                        HomeEffect.ShareArticle(
                            title = it.title,
                            url = it.url,
                        )
                    )
                }
            }
            is HomeIntent.RetryLoad -> {
                fetchArticles(_viewModelState.value.currentNewsCategory)
            }
            is HomeIntent.RefreshArticles -> {
                refreshArticles()
            }
            is HomeIntent.NavigateSearch -> {
                sendEffect(HomeEffect.NavigateSearch)
            }
            is HomeIntent.SummarizeArticle -> {
                summarizeArticle()
            }
            is HomeIntent.DismissSummary -> {
                updateViewModelState { copy(summary = "") }
            }
        }
    }

    private fun changeNewsCategory(newCategory: NewsCategory) {
        updateViewModelState {
            copy(currentNewsCategory = newCategory)
        }
        if (_viewModelState.value.articlesByCategory[newCategory] == null) {
            fetchArticles(newCategory)
        }
    }

    private fun fetchArticles(category: NewsCategory) {
        setLoadingState()
        viewModelScope.launch {
            fetchTopHeadlineArticlesUseCase.invoke(category.value)
                .onSuccess { data ->
                    handleFetchTopHeadlineArticlesSuccess(category, data)
                }
                .onFailure { error ->
                    handleFetchTopHeadlineArticlesError(error)
                }
        }
    }

    private fun setLoadingState() {
        updateViewModelState {
            copy(
                statusType = HomeViewModelState.StatusType.STABLE,
                isLoading = true,
            )
        }
    }

    private fun handleFetchTopHeadlineArticlesSuccess(
        category: NewsCategory,
        data: List<Article>,
    ) {
        updateViewModelState {
            copy(
                isLoading = false,
                articlesByCategory = articlesByCategory + (category to data)
            )
        }
    }

    private fun handleFetchTopHeadlineArticlesError(error: Throwable) {
        Logger.e(TAG, "Failed to fetch articles: ${error.message}", error)
        updateViewModelState {
            copy(
                statusType = HomeViewModelState.StatusType.ERROR,
                isLoading = false,
                error = error as? NewsflowError
            )
        }
    }

    private fun refreshArticles() {
        setRefreshingState()
        viewModelScope.launch {
            val category = _viewModelState.value.currentNewsCategory
            fetchTopHeadlineArticlesUseCase.invoke(category.value, forceRefresh = true)
                .onSuccess { data ->
                    handleRefreshArticlesSuccess(category, data)
                }
                .onFailure { error ->
                    handleRefreshArticlesError(error)
                }
        }
    }

    private fun setRefreshingState() {
        updateViewModelState {
            copy(
                statusType = HomeViewModelState.StatusType.STABLE,
                isRefreshing = true,
            )
        }
    }

    private fun handleRefreshArticlesSuccess(
        category: NewsCategory,
        data: List<Article>,
    ) {
        updateViewModelState {
            copy(
                isRefreshing = false,
                articlesByCategory = articlesByCategory + (category to data)
            )
        }
    }

    private fun handleRefreshArticlesError(error: Throwable) {
        Logger.e(TAG, "Failed to refresh articles: ${error.message}", error)
        updateViewModelState {
            copy(
                statusType = HomeViewModelState.StatusType.ERROR,
                isRefreshing = false,
                error = error as? NewsflowError
            )
        }
    }

    private fun summarizeArticle() {
        val article = _viewModelState.value.selectedArticle ?: return
        viewModelScope.launch {
            summarizeArticleUseCase(article.url)
                .onStart {
                    updateViewModelState { copy(isSummarizing = true, summary = "") }
                }
                .onCompletion {
                    updateViewModelState { copy(isSummarizing = false) }
                }
                .catch { error ->
                    Logger.e(TAG, "Failed to summarize article: ${error.message}", error)
                    val newsflowError = error as? NewsflowError
                        ?: NewsflowError.NetworkError.NetworkFailure(
                            error.message ?: "Unknown error"
                        )
                    sendEffect(HomeEffect.SummaryError(newsflowError))
                }
                .collect { text ->
                    updateViewModelState { copy(summary = summary + text) }
                }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
