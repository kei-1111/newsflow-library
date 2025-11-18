package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.mvi.UiAction
import io.github.kei_1111.newsflow.library.feature.home.model.ArticleUiModel
import io.github.kei_1111.newsflow.library.feature.home.model.NewsCategoryUiModel

sealed interface HomeUiAction : UiAction {
    data class OnClickArticleCard(val articleUiModel: ArticleUiModel) : HomeUiAction

    data class OnSwipNewsCategoryPage(val newsCategoryUiModel: NewsCategoryUiModel) : HomeUiAction

    data object OnClickRetryButton : HomeUiAction
}
