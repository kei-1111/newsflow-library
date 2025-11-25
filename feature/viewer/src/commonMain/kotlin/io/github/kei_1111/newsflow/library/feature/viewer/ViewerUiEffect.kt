package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.mvi.UiEffect

sealed interface ViewerUiEffect : UiEffect {
    data object NavigateBack : ViewerUiEffect
}
