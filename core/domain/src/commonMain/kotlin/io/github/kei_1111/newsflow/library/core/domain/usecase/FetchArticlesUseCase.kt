package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article

interface FetchArticlesUseCase {
    suspend operator fun invoke(category: String): Result<List<Article>>
}
