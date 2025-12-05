package io.github.kei_1111.newsflow.library.feature.search

import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.SearchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.logger.Logger
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel
import io.github.kei_1111.newsflow.library.feature.search.model.SearchOptions
import io.github.kei_1111.newsflow.library.feature.search.model.toDateRange
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchArticlesUseCase: SearchArticlesUseCase,
) : StatefulBaseViewModel<SearchViewModelState, SearchState, SearchIntent, SearchEffect>() {

    override fun createInitialViewModelState(): SearchViewModelState = SearchViewModelState()
    override fun createInitialState(): SearchState = SearchState.Stable()

    init {
        setupDebounceSearch()
    }

    private fun setupDebounceSearch() {
        _viewModelState
            .map { it.query }
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    updateViewModelState { copy(articles = emptyList()) }
                }
            }
            .debounce(DEBOUNCE_MILLIS)
            .filter { it.isNotBlank() }
            .onEach { query -> executeSearch(query) }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                updateViewModelState { copy(query = intent.query) }
            }
            is SearchIntent.ClearQuery -> {
                updateViewModelState { copy(query = "") }
            }
            is SearchIntent.RetrySearch -> {
                retrySearch()
            }
            is SearchIntent.SelectArticle -> {
                sendEffect(SearchEffect.NavigateViewer(intent.article.id))
            }
            is SearchIntent.ShowArticleOverview -> {
                updateViewModelState { copy(selectedArticle = intent.article) }
            }
            is SearchIntent.DismissArticleOverview -> {
                updateViewModelState { copy(selectedArticle = null) }
            }
            is SearchIntent.CopyArticleUrl -> {
                _viewModelState.value.selectedArticle?.let {
                    sendEffect(SearchEffect.CopyUrl(it.url))
                }
            }
            is SearchIntent.ShareArticle -> {
                _viewModelState.value.selectedArticle?.let { article ->
                    sendEffect(
                        SearchEffect.ShareArticle(
                            title = article.title,
                            url = article.url,
                        )
                    )
                }
            }
            is SearchIntent.NavigateBack -> {
                sendEffect(SearchEffect.NavigateBack)
            }
            is SearchIntent.ShowOptionsSheet -> {
                updateViewModelState { copy(isOptionsSheetVisible = true) }
            }
            is SearchIntent.DismissOptionsSheet -> {
                updateViewModelState { copy(isOptionsSheetVisible = false) }
            }
            is SearchIntent.UpdateSortBy -> {
                updateSearchOptions { copy(sortBy = intent.sortBy) }
            }
            is SearchIntent.UpdateDateRange -> {
                updateSearchOptions { copy(dateRangePreset = intent.preset) }
            }
            is SearchIntent.UpdateLanguage -> {
                updateSearchOptions { copy(language = intent.language) }
            }
        }
    }

    private fun updateSearchOptions(update: SearchOptions.() -> SearchOptions) {
        updateViewModelState { copy(searchOptions = searchOptions.update()) }
        retrySearch()
    }

    private fun executeSearch(query: String) {
        setSearchingState()
        val options = _viewModelState.value.searchOptions
        val (from, to) = options.dateRangePreset.toDateRange()
        viewModelScope.launch {
            searchArticlesUseCase(
                query = query,
                sortBy = options.sortBy.apiValue,
                from = from,
                to = to,
                language = options.language.apiValue,
            )
                .onSuccess { articles ->
                    handleSearchSuccess(articles)
                }
                .onFailure { error ->
                    handleSearchError(error)
                }
        }
    }

    private fun setSearchingState() {
        updateViewModelState {
            copy(
                statusType = SearchViewModelState.StatusType.STABLE,
                isSearching = true,
            )
        }
    }

    private fun handleSearchSuccess(articles: List<Article>) {
        updateViewModelState {
            copy(
                isSearching = false,
                articles = articles,
            )
        }
    }

    private fun handleSearchError(error: Throwable) {
        Logger.e(TAG, "Failed to search articles: ${error.message}", error)
        updateViewModelState {
            copy(
                statusType = SearchViewModelState.StatusType.ERROR,
                isSearching = false,
                error = error as? NewsflowError,
            )
        }
    }

    private fun retrySearch() {
        val query = _viewModelState.value.query
        if (query.isNotBlank()) {
            executeSearch(query)
        }
    }

    private companion object {
        const val TAG = "SearchViewModel"
        const val DEBOUNCE_MILLIS = 1000L
    }
}