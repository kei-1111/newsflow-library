package io.github.kei_1111.newsflow.library.core.test.usecase

import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.model.Article

class FakeFetchArticlesUseCase : FetchArticlesUseCase {
    private var result: Result<List<Article>> = Result.success(emptyList())
    var invocationCount = 0
        private set
    var lastCategory: String = ""
        private set

    fun setResult(result: Result<List<Article>>) {
        this.result = result
    }

    override suspend fun invoke(category: String): Result<List<Article>> {
        invocationCount++
        lastCategory = category
        return result
    }
}
