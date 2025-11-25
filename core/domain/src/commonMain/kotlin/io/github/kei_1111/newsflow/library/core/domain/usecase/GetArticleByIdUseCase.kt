package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.model.Article

interface GetArticleByIdUseCase {
    suspend operator fun invoke(id: String): Result<Article>
}
