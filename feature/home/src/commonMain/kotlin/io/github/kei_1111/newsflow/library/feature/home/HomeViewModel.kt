package io.github.kei_1111.newsflow.library.feature.home

import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCase
import io.github.kei_1111.newsflow.library.core.logger.Logger
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

class HomeViewModel(
    private val fetchTopHeadlineArticlesUseCase: FetchTopHeadlineArticlesUseCase
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
                val article = _viewModelState.value.selectedArticle
                article?.let {
                    sendEffect(
                        HomeEffect.ShareArticle(
                            title = article.title,
                            url = article.url,
                        )
                    )
                }
            }
            is HomeIntent.RetryLoad -> {
                fetchArticles(_viewModelState.value.currentNewsCategory)
            }
            is HomeIntent.Refresh -> {
                refreshArticles()
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
            val startMark = TimeSource.Monotonic.markNow()

            fetchTopHeadlineArticlesUseCase.invoke(category.value)
                .onSuccess { data ->
                    handleFetchTopHeadlineArticlesSuccess(category, data, startMark)
                }
                .onFailure { error ->
                    handleFetchTopHeadlineArticlesError(error, startMark)
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

    private suspend fun handleFetchTopHeadlineArticlesSuccess(
        category: NewsCategory,
        data: List<Article>,
        startMark: TimeSource.Monotonic.ValueTimeMark
    ) {
        ensureMinimumLoadingTime(startMark)
        updateViewModelState {
            copy(
                isLoading = false,
                articlesByCategory = articlesByCategory + (category to data)
            )
        }
    }

    private suspend fun handleFetchTopHeadlineArticlesError(
        error: Throwable,
        startMark: TimeSource.Monotonic.ValueTimeMark
    ) {
        Logger.e(TAG, "Failed to fetch articles: ${error.message}", error)
        ensureMinimumLoadingTime(startMark)
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

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
