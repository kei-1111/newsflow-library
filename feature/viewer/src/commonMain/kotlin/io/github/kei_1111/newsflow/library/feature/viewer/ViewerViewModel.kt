package io.github.kei_1111.newsflow.library.feature.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.kei_1111.newsflow.library.core.domain.usecase.GetArticleByIdUseCase
import io.github.kei_1111.newsflow.library.core.logger.Logger
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.StatefulBaseViewModel
import kotlinx.coroutines.launch

class ViewerViewModel(
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    private val savedStateHandle: SavedStateHandle,
) : StatefulBaseViewModel<ViewerViewModelState, ViewerUiState, ViewerUiAction, ViewerUiEffect>() {

    override fun createInitialViewModelState(): ViewerViewModelState = ViewerViewModelState()
    override fun createInitialUiState(): ViewerUiState = ViewerUiState.Init

    init {
        getArticle()
    }

    override fun onUiAction(uiAction: ViewerUiAction) {
        when (uiAction) {
            ViewerUiAction.OnClickNavigateHome -> {
                sendUiEffect(ViewerUiEffect.NavigateHome)
            }
        }
    }

    private fun getArticle() {
        val articleId = savedStateHandle.get<String>("articleId")
        if (articleId == null) {
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
