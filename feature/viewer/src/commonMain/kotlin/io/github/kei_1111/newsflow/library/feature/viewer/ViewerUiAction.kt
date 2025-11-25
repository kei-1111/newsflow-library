package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.mvi.UiAction

sealed interface ViewerUiAction : UiAction {
    data object OnClickNavigateHome : ViewerUiAction
}
