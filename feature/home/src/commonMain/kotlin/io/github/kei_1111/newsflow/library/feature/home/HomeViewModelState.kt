package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState

data class HomeViewModelState(
    val statusType: StatusType = StatusType.STABLE,
    val isLoading: Boolean = false,
    val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
    val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    val error: NewsflowError? = null,
) : ViewModelState<HomeUiState> {
    enum class StatusType { STABLE, ERROR }

    override fun toState(): HomeUiState = when (statusType) {
        StatusType.STABLE -> HomeUiState.Stable(
            isLoading = isLoading,
            currentNewsCategory = currentNewsCategory,
            articlesByCategory = articlesByCategory
        )

        StatusType.ERROR -> HomeUiState.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
        )
    }
}
