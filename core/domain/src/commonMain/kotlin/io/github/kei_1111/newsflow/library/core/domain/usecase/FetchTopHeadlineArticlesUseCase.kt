package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.model.Article

interface FetchTopHeadlineArticlesUseCase {
    suspend operator fun invoke(category: String): Result<List<Article>>
}
