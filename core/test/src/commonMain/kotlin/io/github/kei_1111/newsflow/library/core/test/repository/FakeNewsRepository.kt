package io.github.kei_1111.newsflow.library.core.test.repository

import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article

class FakeNewsRepository : NewsRepository {
    private var result: Result<List<Article>> = Result.success(emptyList())
    var invocationCount = 0
        private set

    fun setResult(result: Result<List<Article>>) {
        this.result = result
    }

    override suspend fun fetchArticles(category: String): Result<List<Article>> {
        invocationCount++
        return result
    }
}