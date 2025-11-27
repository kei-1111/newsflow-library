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
) : StatefulBaseViewModel<HomeViewModelState, HomeUiState, HomeUiAction, HomeUiEffect>() {

    override fun createInitialViewModelState(): HomeViewModelState = HomeViewModelState()
    override fun createInitialUiState(): HomeUiState = HomeUiState.Stable()

    init {
        fetchArticles(_viewModelState.value.currentNewsCategory)
    }

    override fun onUiAction(uiAction: HomeUiAction) {
        when (uiAction) {
            is HomeUiAction.OnClickArticleCard -> {
                sendUiEffect(HomeUiEffect.NavigateViewer(uiAction.article.id))
            }
            is HomeUiAction.OnSwipNewsCategoryPage -> {
                changeNewsCategory(uiAction.newsCategory)
            }
            is HomeUiAction.OnClickNewsCategoryTag -> {
                changeNewsCategory(uiAction.newsCategory)
            }
            is HomeUiAction.OnClickMoreBottom -> {
                updateViewModelState { copy(selectedArticle = uiAction.article) }
            }
            is HomeUiAction.OnDismissArticleOverviewBottomSheet -> {
                updateViewModelState { copy(selectedArticle = null) }
            }
            is HomeUiAction.OnClickCopyUrlButton -> {
                _viewModelState.value.selectedArticle?.let {
                    sendUiEffect(HomeUiEffect.CopyUrl(it.url))
                }
            }
            is HomeUiAction.OnClickShareButton -> {
                val article = _viewModelState.value.selectedArticle
                article?.let {
                    sendUiEffect(
                        HomeUiEffect.ShareArticle(
                            title = article.title,
                            url = article.url,
                        )
                    )
                }
            }
            is HomeUiAction.OnClickRetryButton -> {
                fetchArticles(_viewModelState.value.currentNewsCategory)
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

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
