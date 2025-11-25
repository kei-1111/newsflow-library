package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.UiState

sealed interface ViewerUiState : UiState {
    data object Init : ViewerUiState

    data object Loading : ViewerUiState

    data class Stable(
        val viewingArticle: Article,
    ) : ViewerUiState

    data class Error(
        val error: NewsflowError,
    ) : ViewerUiState
}
