package io.github.kei_1111.newsflow.library.core.data.repository

import io.github.kei_1111.newsflow.library.core.data.mapper.toArticles
import io.github.kei_1111.newsflow.library.core.data.util.toNewsflowError
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.network.api.NewsApiService

internal class NewsRepositoryImpl(
    private val newsApiService: NewsApiService,
) : NewsRepository {
    override suspend fun fetchArticles(
        category: String,
    ): Result<List<Article>> = newsApiService.fetchTopHeadlines(category).fold(
        onSuccess = { Result.success(it.toArticles()) },
        onFailure = { Result.failure(it.toNewsflowError()) }
    )
}