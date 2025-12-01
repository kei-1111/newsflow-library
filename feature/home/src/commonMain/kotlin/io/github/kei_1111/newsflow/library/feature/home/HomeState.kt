package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.State

sealed interface HomeState : State {
    data class Stable(
        val isLoading: Boolean = false,
        val selectedArticle: Article? = null,
        val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
        val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    ) : HomeState

    data class Error(
        val error: NewsflowError,
    ) : HomeState
}