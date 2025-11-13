package io.github.kei_1111.newsflow.library.core.data.repository

import io.github.kei_1111.newsflow.library.core.model.Article

interface NewsRepository {
    suspend fun fetchArticles(category: String): Result<List<Article>>
}