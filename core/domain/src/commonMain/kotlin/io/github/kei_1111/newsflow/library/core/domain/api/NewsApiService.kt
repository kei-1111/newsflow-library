package io.github.kei_1111.newsflow.library.core.domain.api

import io.github.kei_1111.newsflow.library.core.model.Article

interface NewsApiService {
    suspend fun fetchTopHeadlines(category: String): List<Article>
}
