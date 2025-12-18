package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState

data class ViewerViewModelState(
    val statusType: StatusType = StatusType.INIT,
    val isWebViewLoading: Boolean = true,
    val viewingArticle: Article? = null,
    val error: NewsflowError? = null,
    val isSummarizing: Boolean = false,
    val summary: String = "",
) : ViewModelState<ViewerState> {
    enum class StatusType { INIT, LOADING, STABLE, ERROR }

    override fun toState(): ViewerState = when (statusType) {
        StatusType.INIT -> ViewerState.Init

        StatusType.LOADING -> ViewerState.Loading

        StatusType.STABLE -> ViewerState.Stable(
            isWebViewLoading = isWebViewLoading,
            viewingArticle = requireNotNull(viewingArticle) { "Article must not be null when statusType is STABLE" },
            isSummarizing = isSummarizing,
            summary = summary.trim(),
        )

        StatusType.ERROR -> ViewerState.Error(
            error = requireNotNull(error) { "Error must not be null when statusType is ERROR" }
        )
    }
}
