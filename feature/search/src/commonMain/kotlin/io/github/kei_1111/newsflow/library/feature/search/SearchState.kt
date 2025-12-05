package io.github.kei_1111.newsflow.library.feature.search

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.State
import io.github.kei_1111.newsflow.library.feature.search.model.SearchOptions

sealed interface SearchState : State {
    data class Stable(
        val query: String = "",
        val isSearching: Boolean = false,
        val articles: List<Article> = emptyList(),
        val selectedArticle: Article? = null,
        val searchOptions: SearchOptions = SearchOptions(),
        val isOptionsSheetVisible: Boolean = false,
    ) : SearchState

    data class Error(
        val error: NewsflowError,
    ) : SearchState
}