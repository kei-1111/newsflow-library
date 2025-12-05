package io.github.kei_1111.newsflow.library.feature.search

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState
import io.github.kei_1111.newsflow.library.feature.search.model.SearchOptions

data class SearchViewModelState(
    val statusType: StatusType = StatusType.STABLE,
    val query: String = "",
    val isSearching: Boolean = false,
    val articles: List<Article> = emptyList(),
    val selectedArticle: Article? = null,
    val error: NewsflowError? = null,
    val searchOptions: SearchOptions = SearchOptions(),
    val isOptionsSheetVisible: Boolean = false,
) : ViewModelState<SearchState> {
    enum class StatusType { STABLE, ERROR }

    override fun toState(): SearchState = when (statusType) {
        StatusType.STABLE -> SearchState.Stable(
            query = query,
            isSearching = isSearching,
            articles = articles,
            selectedArticle = selectedArticle,
            searchOptions = searchOptions,
            isOptionsSheetVisible = isOptionsSheetVisible,
        )

        StatusType.ERROR -> SearchState.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" },
        )
    }
}
