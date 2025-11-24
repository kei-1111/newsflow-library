package io.github.kei_1111.newsflow.library.core.test.network

import io.github.kei_1111.newsflow.library.core.network.api.NewsApiService
import io.github.kei_1111.newsflow.library.core.network.model.NewsResponse

class FakeNewsApiService : NewsApiService {
    private var result: Result<NewsResponse> = Result.success(
        NewsResponse(
            status = "ok",
            totalResults = 0,
            articles = emptyList(),
        ),
    )
    var invocationCount = 0
        private set

    fun setResult(result: Result<NewsResponse>) {
        this.result = result
    }

    override suspend fun fetchTopHeadlines(category: String): Result<NewsResponse> {
        invocationCount++
        return result
    }
}