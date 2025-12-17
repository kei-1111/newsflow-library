package io.github.kei_1111.newsflow.library.feature.search

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.mvi.Intent
import io.github.kei_1111.newsflow.library.feature.search.model.DateRangePreset
import io.github.kei_1111.newsflow.library.feature.search.model.SearchLanguage
import io.github.kei_1111.newsflow.library.feature.search.model.SortBy

sealed interface SearchIntent : Intent {
    data class UpdateQuery(val query: String) : SearchIntent
    data object ClearQuery : SearchIntent
    data object RetrySearch : SearchIntent
    data class NavigateViewer(val article: Article) : SearchIntent
    data class ShowArticleOverview(val article: Article) : SearchIntent
    data object DismissArticleOverview : SearchIntent
    data object CopyArticleUrl : SearchIntent
    data object ShareArticle : SearchIntent
    data object NavigateBack : SearchIntent

    // Search options
    data object ShowOptionsSheet : SearchIntent
    data object DismissOptionsSheet : SearchIntent
    data class UpdateSortBy(val sortBy: SortBy) : SearchIntent
    data class UpdateDateRange(val preset: DateRangePreset) : SearchIntent
    data class UpdateLanguage(val language: SearchLanguage) : SearchIntent

    // Summarize
    data object SummarizeArticle : SearchIntent
    data object DismissSummary : SearchIntent
}
