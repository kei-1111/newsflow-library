package io.github.kei_1111.newsflow.library.core.network.api

import io.github.kei_1111.newsflow.library.core.network.model.NewsResponse

interface NewsApiService {
    suspend fun fetchTopHeadlines(category: String): Result<NewsResponse>
    suspend fun searchArticles(
        query: String,
        sortBy: String? = null,
        from: String? = null,
        to: String? = null,
        language: String? = null,
    ): Result<NewsResponse>
}
