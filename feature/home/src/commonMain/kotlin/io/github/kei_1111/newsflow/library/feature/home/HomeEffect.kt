package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.Effect

sealed interface HomeEffect : Effect {
    data class NavigateViewer(val id: String) : HomeEffect
    data object NavigateSearch : HomeEffect
    data class CopyUrl(val url: String) : HomeEffect
    data class ShareArticle(val title: String, val url: String) : HomeEffect
    data class SummaryError(val error: NewsflowError) : HomeEffect
}
