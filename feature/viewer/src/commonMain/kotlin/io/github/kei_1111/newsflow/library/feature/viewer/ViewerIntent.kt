package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.mvi.Intent

sealed interface ViewerIntent : Intent {
    data object NavigateBack : ViewerIntent
    data object ShareArticle : ViewerIntent
    data object StartWebViewLoading : ViewerIntent
    data object FinishWebViewLoading : ViewerIntent
}
