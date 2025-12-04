package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError

internal class SearchArticlesUseCaseImpl(
    private val newsRepository: NewsRepository,
) : SearchArticlesUseCase {
    override suspend operator fun invoke(query: String): Result<List<Article>> {
        if (query.isBlank()) {
            return Result.failure(
                NewsflowError.InternalError.InvalidParameter("Search query cannot be empty")
            )
        }
        return newsRepository.searchArticles(query.trim())
    }
}