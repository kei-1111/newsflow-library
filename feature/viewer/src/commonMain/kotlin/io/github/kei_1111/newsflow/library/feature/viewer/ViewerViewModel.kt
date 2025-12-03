package io.github.kei_1111.newsflow.library.feature.viewer

import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.GetArticleByIdUseCase
import io.github.kei_1111.newsflow.library.core.logger.Logger
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel
import kotlinx.coroutines.launch

class ViewerViewModel(
    private val articleId: String,
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
) : StatefulBaseViewModel<ViewerViewModelState, ViewerState, ViewerIntent, ViewerEffect>() {

    override fun createInitialViewModelState(): ViewerViewModelState = ViewerViewModelState()
    override fun createInitialState(): ViewerState = ViewerState.Init

    init {
        getArticle()
    }

    override fun onIntent(intent: ViewerIntent) {
        when (intent) {
            ViewerIntent.NavigateBack -> {
                sendEffect(ViewerEffect.NavigateBack)
            }
            is ViewerIntent.ShareArticle -> {
                _viewModelState.value.viewingArticle?.let {
                    sendEffect(
                        ViewerEffect.ShareArticle(
                            title = it.title,
                            url = it.url,
                        )
                    )
                }
            }
            is ViewerIntent.StartWebViewLoading -> {
                updateViewModelState { copy(isWebViewLoading = true) }
            }
            is ViewerIntent.FinishWebViewLoading -> {
                updateViewModelState { copy(isWebViewLoading = false) }
            }
        }
    }

    private fun getArticle() {
        if (articleId.isBlank()) {
            handleGetArticleError(NewsflowError.InternalError.InvalidParameter("Article ID is missing"))
            return
        }

        setLoadingState()
        viewModelScope.launch {
            getArticleByIdUseCase(articleId)
                .onSuccess { article ->
                    handleGetArticleSuccess(article)
                }
                .onFailure { error ->
                    handleGetArticleError(error)
                }
        }
    }

    private fun setLoadingState() {
        updateViewModelState {
            copy(statusType = ViewerViewModelState.StatusType.LOADING)
        }
    }

    private fun handleGetArticleSuccess(article: Article) {
        updateViewModelState {
            copy(
                statusType = ViewerViewModelState.StatusType.STABLE,
                viewingArticle = article
            )
        }
    }

    private fun handleGetArticleError(error: Throwable) {
        Logger.e(TAG, "Failed to get article: ${error.message}", error)
        updateViewModelState {
            copy(
                statusType = ViewerViewModelState.StatusType.ERROR,
                error = error as? NewsflowError
            )
        }
    }

    private companion object {
        const val TAG = "ViewerViewModel"
    }
}
