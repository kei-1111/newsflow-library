package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.mvi.UiAction

sealed interface HomeUiAction : UiAction {
    data class OnClickArticleCard(val article: Article) : HomeUiAction

    data class OnSwipNewsCategoryPage(val newsCategory: NewsCategory) : HomeUiAction

    data object OnClickRetryButton : HomeUiAction
}
