package io.github.kei_1111.newsflow.library.feature.home

import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.exception.NewsflowError
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(
    private val fetchArticlesUseCase: FetchArticlesUseCase
) : StatefulBaseViewModel<HomeViewModelState, HomeUiState, HomeUiAction, HomeUiEffect>() {

    override fun createInitialViewModelState(): HomeViewModelState = HomeViewModelState()
    override fun createInitialUiState(): HomeUiState = HomeUiState.Init

    init {
        fetchArticles(_viewModelState.value.currentNewsCategory)
    }

    private fun fetchArticles(category: NewsCategory) {
        updateViewModelState { copy(statusType = HomeViewModelState.StatusType.LOADING) }
        viewModelScope.launch {
            val startMark = TimeSource.Monotonic.markNow()

            fetchArticlesUseCase.invoke(category.value)
                .onSuccess { data ->
                    ensureMinimumLoadingTime(startMark)
                    updateViewModelState {
                        copy(
                            statusType = HomeViewModelState.StatusType.STABLE,
                            articlesByCategory = articlesByCategory + (category to data)
                        )
                    }
                }
                .onFailure { error ->
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

    private suspend fun ensureMinimumLoadingTime(startMark: TimeSource.Monotonic.ValueTimeMark) {
        val elapsed = startMark.elapsedNow()
        if (elapsed < MIN_LOADING_TIME) {
            delay(MIN_LOADING_TIME - elapsed)
        }
    }

    override fun onAction(action: HomeUiAction) {
        when(action) {
            is HomeUiAction.OnClickArticleCard -> {
                sendEffect(HomeUiEffect.NavigateViewer(action.articleUiModel.url))
            }

            is HomeUiAction.OnSwipNewsCategoryPage -> {
                val newCategory = action.newsCategoryUiModel.toNewsCategory()

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
        val MIN_LOADING_TIME = 500.milliseconds
    }
}