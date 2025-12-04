package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.model.Article

interface SearchArticlesUseCase {
    suspend operator fun invoke(query: String): Result<List<Article>>
}