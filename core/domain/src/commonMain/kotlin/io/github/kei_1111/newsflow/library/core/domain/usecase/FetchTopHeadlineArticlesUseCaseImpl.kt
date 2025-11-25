package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article

internal class FetchTopHeadlineArticlesUseCaseImpl(
    private val newsRepository: NewsRepository,
) : FetchTopHeadlineArticlesUseCase {
    override suspend operator fun invoke(
        category: String,
        forceRefresh: Boolean,
    ): Result<List<Article>> =
        newsRepository.fetchArticles(category, forceRefresh)
}
