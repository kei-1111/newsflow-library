package io.github.kei_1111.newsflow.library.feature.search

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.State

sealed interface SearchState : State {
    data class Stable(
        val query: String = "",
        val isSearching: Boolean = false,
        val articles: List<Article> = emptyList(),
        val selectedArticle: Article? = null,
    ) : SearchState

    data class Error(
        val query: String,
        val error: NewsflowError,
    ) : SearchState
}