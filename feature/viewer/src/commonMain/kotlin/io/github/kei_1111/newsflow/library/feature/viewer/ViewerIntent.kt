package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.mvi.Intent

sealed interface ViewerIntent : Intent {
    data object NavigateBack : ViewerIntent
    data class ShareArticle(val article: Article) : ViewerIntent
    data object StartWebViewLoading : ViewerIntent
    data object FinishWebViewLoading : ViewerIntent
}
