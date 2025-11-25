package io.github.kei_1111.newsflow.library.feature.viewer.di

import io.github.kei_1111.newsflow.library.feature.viewer.ViewerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewerModule = module {
    viewModel { (articleId: String) ->
        ViewerViewModel(
            articleId = articleId,
            getArticleByIdUseCase = get(),
        )
    }
}
