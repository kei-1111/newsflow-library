package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.mvi.UiAction

sealed interface ViewerUiAction : UiAction {
    data object OnClickBackButton : ViewerUiAction
    data class OnClickShareButton(val article: Article) : ViewerUiAction
}
