package io.github.kei_1111.newsflow.library.core.test.usecase

import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCase
import io.github.kei_1111.newsflow.library.core.model.Article

class FakeFetchTopHeadlineArticlesUseCase : FetchTopHeadlineArticlesUseCase {
    private var result: Result<List<Article>> = Result.success(emptyList())
    var invocationCount = 0
        private set

    fun setResult(result: Result<List<Article>>) {
        this.result = result
    }

    override suspend fun invoke(category: String): Result<List<Article>> {
        invocationCount++
        return result
    }
}
