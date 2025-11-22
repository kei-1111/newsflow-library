package io.github.kei_1111.newsflow.library.feature.home

import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.logger.Logger
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

class HomeViewModel(
    private val fetchArticlesUseCase: FetchArticlesUseCase
) : StatefulBaseViewModel<HomeViewModelState, HomeUiState, HomeUiAction, HomeUiEffect>() {

    override fun createInitialViewModelState(): HomeViewModelState = HomeViewModelState()
    override fun createInitialUiState(): HomeUiState = HomeUiState.Init

    init {
        fetchArticles(_viewModelState.value.currentNewsCategory)
    }

    private fun fetchArticles(category: NewsCategory) {
        updateViewModelState {
            copy(
                statusType = HomeViewModelState.StatusType.STABLE,
                isLoading = true,
            )
        }
        viewModelScope.launch {
            val startMark = TimeSource.Monotonic.markNow()

            fetchArticlesUseCase.invoke(category.value)
                .onSuccess { data ->
                    ensureMinimumLoadingTime(startMark)
                    updateViewModelState { copy(articlesByCategory = articlesByCategory + (category to data)) }
                }
                .onFailure { error ->
                    Logger.e(TAG, "Failed to fetch articles: ${error.message}", error)

                    ensureMinimumLoadingTime(startMark)
                    updateViewModelState {
                        copy(
                            statusType = HomeViewModelState.StatusType.ERROR,
                            error = error as? NewsflowError
                        )
                    }
                }
        }
    }

    override fun onUiAction(uiAction: HomeUiAction) {
        when (uiAction) {
            is HomeUiAction.OnClickArticleCard -> {
                sendUiEffect(HomeUiEffect.NavigateViewer(uiAction.article.url))
            }

            is HomeUiAction.OnSwipNewsCategoryPage -> {
                val newCategory = uiAction.newsCategory

                updateViewModelState {
                    copy(currentNewsCategory = newCategory)
                }
                if (_viewModelState.value.articlesByCategory[newCategory] == null) {
                    fetchArticles(newCategory)
                }
            }

            is HomeUiAction.OnClickNewsCategoryTag -> {
                val newCategory = uiAction.newsCategory

                updateViewModelState {
                    copy(currentNewsCategory = newCategory)
                }
                if (_viewModelState.value.articlesByCategory[newCategory] == null) {
                    fetchArticles(newCategory)
                }
            }

            is HomeUiAction.OnClickRetryButton -> {
                fetchArticles(_viewModelState.value.currentNewsCategory)
            }
        }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
