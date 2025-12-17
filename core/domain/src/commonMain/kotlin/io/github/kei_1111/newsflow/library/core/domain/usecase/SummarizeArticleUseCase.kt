package io.github.kei_1111.newsflow.library.core.domain.usecase

import kotlinx.coroutines.flow.Flow

interface SummarizeArticleUseCase {
    operator fun invoke(articleUrl: String): Flow<String>
}
