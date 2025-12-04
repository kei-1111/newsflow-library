package io.github.kei_1111.newsflow.library.feature.search

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.mvi.Intent

sealed interface SearchIntent : Intent {
    data class UpdateQuery(val query: String) : SearchIntent
    data object ExecuteSearch : SearchIntent
    data object ClearQuery : SearchIntent
    data object RetrySearch : SearchIntent
    data class SelectArticle(val article: Article) : SearchIntent
    data class ShowArticleOverview(val article: Article) : SearchIntent
    data object DismissArticleOverview : SearchIntent
    data object CopyArticleUrl : SearchIntent
    data object ShareArticle : SearchIntent
    data object NavigateBack : SearchIntent
}