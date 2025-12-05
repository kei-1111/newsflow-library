package io.github.kei_1111.newsflow.library.core.data.repository

import io.github.kei_1111.newsflow.library.core.model.Article

interface NewsRepository {
    suspend fun fetchArticles(
        category: String,
        forceRefresh: Boolean = false,
    ): Result<List<Article>>

    suspend fun getArticleById(id: String): Result<Article>

    suspend fun searchArticles(
        query: String,
        sortBy: String? = null,
        from: String? = null,
        to: String? = null,
        language: String? = null,
    ): Result<List<Article>>
}
