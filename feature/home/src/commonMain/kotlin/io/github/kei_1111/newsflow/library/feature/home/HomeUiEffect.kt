package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.mvi.UiEffect

sealed interface HomeUiEffect : UiEffect {
    data class NavigateViewer(val url: String) : HomeUiEffect
}