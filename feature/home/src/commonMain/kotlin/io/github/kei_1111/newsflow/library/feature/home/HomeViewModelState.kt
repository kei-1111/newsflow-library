package io.github.kei_1111.newsflow.library.feature.home

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsCategory
import io.github.kei_1111.newsflow.library.core.exception.NewsflowError
import io.github.kei_1111.newsflow.library.core.mvi.stateful.ViewModelState
import io.github.kei_1111.newsflow.library.feature.home.model.ArticleUiModel
import io.github.kei_1111.newsflow.library.feature.home.model.NewsCategoryUiModel

data class HomeViewModelState(
    val statusType: StatusType = StatusType.IDLE,
    val currentNewsCategory: NewsCategory = NewsCategory.GENERAL,
    val articlesByCategory: Map<NewsCategory, List<Article>> = emptyMap(),
    val error: NewsflowError? = null,
) : ViewModelState<HomeUiState> {
    enum class StatusType { IDLE, LOADING, STABLE, ERROR}

    override fun toState(): HomeUiState = when(statusType) {
        StatusType.IDLE -> HomeUiState.Init

        StatusType.LOADING -> HomeUiState.Loading

        StatusType.STABLE -> HomeUiState.Stable(
            currentNewsCategoryUiModel = NewsCategoryUiModel.convert(currentNewsCategory),
            articlesByCategory = articlesByCategory
                .mapKeys { (category, _) ->
                    NewsCategoryUiModel.convert(category)
                }.mapValues { (_, articles) ->
                    articles.map { ArticleUiModel.convert(it) }
                }
        )

        StatusType.ERROR -> HomeUiState.Error(
            message = when (error) {
                is NewsflowError.Unauthorized -> "認証エラーが発生しました。アプリを再起動してください。"
                is NewsflowError.RateLimitExceeded -> "リクエストが多すぎます。しばらく待ってからお試しください。"
                is NewsflowError.BadRequest -> "リクエストエラーが発生しました。"
                is NewsflowError.ServerError -> "サーバーエラーが発生しました。しばらく待ってからお試しください。"
                is NewsflowError.NetworkFailure -> "ネットワークに接続できません。インターネット接続を確認してください。"
                else -> "エラーが発生しました。"
            }
        )
    }
}
