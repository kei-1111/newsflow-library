package io.github.kei_1111.newsflow.library.feature.search

import io.github.kei_1111.newsflow.library.core.mvi.Effect

sealed interface SearchEffect : Effect {
    data class NavigateViewer(val articleId: String) : SearchEffect
    data object NavigateBack : SearchEffect
    data class CopyUrl(val url: String) : SearchEffect
    data class ShareArticle(val title: String, val url: String) : SearchEffect
}