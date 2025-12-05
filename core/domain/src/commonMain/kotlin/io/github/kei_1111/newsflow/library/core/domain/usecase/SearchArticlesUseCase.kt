package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.model.Article

interface SearchArticlesUseCase {
    suspend operator fun invoke(
        query: String,
        sortBy: String? = null,
        from: String? = null,
        to: String? = null,
        language: String? = null,
    ): Result<List<Article>>
}