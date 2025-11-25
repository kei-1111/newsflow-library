package io.github.kei_1111.newsflow.library.core.test.repository

import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.model.Article

class FakeNewsRepository : NewsRepository {
    private var fetchResult: Result<List<Article>> = Result.success(emptyList())
    private var getByIdResult: Result<Article> = Result.failure(Exception("Not initialized"))

    var fetchInvocationCount = 0
        private set
    var getByIdInvocationCount = 0
        private set

    var lastFetchCategory: String? = null
        private set
    var lastFetchForceRefresh: Boolean? = null
        private set
    var lastGetByIdArticleId: String? = null
        private set

    fun setFetchResult(result: Result<List<Article>>) {
        this.fetchResult = result
    }

    fun setGetByIdResult(result: Result<Article>) {
        this.getByIdResult = result
    }

    override suspend fun fetchArticles(
        category: String,
        forceRefresh: Boolean,
    ): Result<List<Article>> {
        fetchInvocationCount++
        lastFetchCategory = category
        lastFetchForceRefresh = forceRefresh
        return fetchResult
    }

    override suspend fun getArticleById(id: String): Result<Article> {
        getByIdInvocationCount++
        lastGetByIdArticleId = id
        return getByIdResult
    }
}
