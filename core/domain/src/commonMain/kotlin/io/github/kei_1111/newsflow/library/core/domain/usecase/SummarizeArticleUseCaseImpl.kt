package io.github.kei_1111.newsflow.library.core.domain.usecase

import io.github.kei_1111.newsflow.library.core.data.repository.SummaryRepository
import kotlinx.coroutines.flow.Flow

internal class SummarizeArticleUseCaseImpl(
    private val summaryRepository: SummaryRepository,
) : SummarizeArticleUseCase {
    override operator fun invoke(articleUrl: String): Flow<String> =
        summaryRepository.summarizeArticle(articleUrl)
}
