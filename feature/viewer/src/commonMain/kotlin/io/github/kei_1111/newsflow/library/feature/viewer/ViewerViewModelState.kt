package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState

data class ViewerViewModelState(
    val statusType: StatusType = StatusType.INIT,
    val viewingArticle: Article? = null,
    val error: NewsflowError? = null,
) : ViewModelState<ViewerUiState> {
    enum class StatusType { INIT, LOADING, STABLE, ERROR }

    override fun toState(): ViewerUiState = when (statusType) {
        StatusType.INIT -> ViewerUiState.Init

        StatusType.LOADING -> ViewerUiState.Loading

        StatusType.STABLE -> ViewerUiState.Stable(
            viewingArticle = requireNotNull(viewingArticle) { "Article must not be null when statusType is STABLE" }
        )

        StatusType.ERROR -> ViewerUiState.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
        )
    }
}
