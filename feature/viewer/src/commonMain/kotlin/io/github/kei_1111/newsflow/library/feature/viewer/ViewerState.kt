package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.State

sealed interface ViewerState : State {
    data object Init : ViewerState

    data object Loading : ViewerState

    data class Stable(
        val isWebViewLoading: Boolean,
        val viewingArticle: Article,
        val isSummarizing: Boolean = false,
        val summary: String = "",
    ) : ViewerState

    data class Error(
        val error: NewsflowError,
    ) : ViewerState
}
