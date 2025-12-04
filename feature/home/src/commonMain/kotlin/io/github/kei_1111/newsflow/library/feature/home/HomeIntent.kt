package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.mvi.Intent

sealed interface HomeIntent : Intent {
    data class SelectArticle(val article: Article) : HomeIntent
    data class ChangeCategory(val newsCategory: NewsCategory) : HomeIntent
    data class ShowArticleOverview(val article: Article) : HomeIntent
    data object DismissArticleOverview : HomeIntent
    data object CopyArticleUrl : HomeIntent
    data object ShareArticle : HomeIntent
    data object RetryLoad : HomeIntent
    data object Refresh : HomeIntent
    data object NavigateSearch : HomeIntent
}
