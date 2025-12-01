package io.github.kei_1111.newsflow.library.feature.viewer

import io.github.kei_1111.newsflow.library.core.mvi.Effect

sealed interface ViewerEffect : Effect {
    data object NavigateBack : ViewerEffect
    data class ShareArticle(val title: String, val url: String) : ViewerEffect
}