package io.github.kei_1111.newsflow.library.core.test.usecase

import io.github.kei_1111.newsflow.library.core.domain.usecase.GetArticleByIdUseCase
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.test.model.createTestArticle

class FakeGetArticleByIdUseCase : GetArticleByIdUseCase {
    private var result: Result<Article> = Result.success(createTestArticle(1))
    var invocationCount = 0
        private set
    var lastInvokedId: String? = null
        private set

    fun setResult(result: Result<Article>) {
        this.result = result
    }

    override suspend fun invoke(id: String): Result<Article> {
        invocationCount++
        lastInvokedId = id
        return result
    }
}
