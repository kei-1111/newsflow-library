package io.github.kei_1111.newsflow.library.feature.search

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState

data class SearchViewModelState(
    val statusType: StatusType = StatusType.STABLE,
    val query: String = "",
    val isSearching: Boolean = false,
    val articles: List<Article> = emptyList(),
    val selectedArticle: Article? = null,
    val error: NewsflowError? = null,
) : ViewModelState<SearchState> {
    enum class StatusType { STABLE, ERROR }

    override fun toState(): SearchState = when (statusType) {
        StatusType.STABLE -> SearchState.Stable(
            query = query,
            isSearching = isSearching,
            articles = articles,
            selectedArticle = selectedArticle,
        )

        StatusType.ERROR -> SearchState.Error(
            query = query,
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" },
        )
    }
}