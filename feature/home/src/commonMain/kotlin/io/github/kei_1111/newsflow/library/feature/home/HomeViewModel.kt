package io.github.kei_1111.newsflow.library.feature.home

import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.exception.NewsflowError
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowErrorType
import io.github.kei_1111.newsflow.library.core.model.toType
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
                    // TODO: KMP対応のロガーで出力
                    val errorType = when (val newsflowError = error as? NewsflowError) {
                        null -> {
                            // 未知のエラーの場合
                            // TODO: ログ出力
                            NewsflowErrorType.Unknown
                        }
                        else -> newsflowError.toType()
                    }
                    ensureMinimumLoadingTime(startMark)
                    updateViewModelState {
                        copy(
                            statusType = HomeViewModelState.StatusType.ERROR,
                            errorType = errorType
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

            is HomeUiAction.OnClickRetryButton -> {
                fetchArticles(_viewModelState.value.currentNewsCategory)
            }
        }
    }
}
