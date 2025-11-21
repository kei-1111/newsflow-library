package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.UiState

sealed interface HomeUiState : UiState {
    data object Init : HomeUiState

    data object Loading : HomeUiState

    data class Stable(
        val currentNewsCategory: NewsCategory,
        val articlesByCategory: Map<NewsCategory, List<Article>>,
    ) : HomeUiState

    data class Error(
        val error: NewsflowError,
    ) : HomeUiState
}
