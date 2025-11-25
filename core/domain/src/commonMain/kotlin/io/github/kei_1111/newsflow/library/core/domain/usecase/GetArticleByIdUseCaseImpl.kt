package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article

internal class GetArticleByIdUseCaseImpl(
    private val newsRepository: NewsRepository,
) : GetArticleByIdUseCase {
    override suspend operator fun invoke(id: String): Result<Article> =
        newsRepository.getArticleById(id)
}
