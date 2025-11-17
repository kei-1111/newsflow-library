package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.mvi.stateful.UiState
import io.github.kei_1111.newsflow.library.feature.home.model.ArticleUiModel
import io.github.kei_1111.newsflow.library.feature.home.model.NewsCategoryUiModel

sealed interface HomeUiState : UiState {
    data object Init: HomeUiState

    data object Loading: HomeUiState

    data class Stable(
        val currentNewsCategoryUiModel: NewsCategoryUiModel,
        val articlesByCategory: Map<NewsCategoryUiModel, List<ArticleUiModel>>,
    ): HomeUiState

    data class Error(
        val message: String,
    ): HomeUiState
}