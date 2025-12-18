package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState

data class HomeViewModelState(
    val statusType: StatusType = StatusType.STABLE,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedArticle: Article? = null,
    val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
    val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    val error: NewsflowError? = null,
    val isSummarizing: Boolean = false,
    val summary: String = "",
) : ViewModelState<HomeState> {
    enum class StatusType { STABLE, ERROR }

    override fun toState(): HomeState = when (statusType) {
        StatusType.STABLE -> HomeState.Stable(
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            selectedArticle = selectedArticle,
            currentNewsCategory = currentNewsCategory,
            articlesByCategory = articlesByCategory,
            isSummarizing = isSummarizing,
            summary = summary.trim(),
        )

        StatusType.ERROR -> HomeState.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
        )
    }
}
