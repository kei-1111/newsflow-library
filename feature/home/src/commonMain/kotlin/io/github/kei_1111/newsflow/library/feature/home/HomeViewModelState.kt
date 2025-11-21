package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowErrorType
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState

data class HomeViewModelState(
    val statusType: StatusType = StatusType.IDLE,
    val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
    val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    val errorType: NewsflowErrorType? = null,
) : ViewModelState<HomeUiState> {
    enum class StatusType { IDLE, LOADING, STABLE, ERROR }

    override fun toState(): HomeUiState = when (statusType) {
        StatusType.IDLE -> HomeUiState.Init

        StatusType.LOADING -> HomeUiState.Loading

        StatusType.STABLE -> HomeUiState.Stable(
            currentNewsCategory = currentNewsCategory,
            articlesByCategory = articlesByCategory
        )

        StatusType.ERROR -> HomeUiState.Error(
            errorType = requireNotNull(errorType) { "Error must not be null when statusType is ERROR" }
        )
    }
}
