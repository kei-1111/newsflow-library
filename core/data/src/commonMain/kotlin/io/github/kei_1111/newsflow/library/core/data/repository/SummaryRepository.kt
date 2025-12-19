package io.github.kei_1111.newsflow.library.core.data.repository

import kotlinx.coroutines.flow.Flow

interface SummaryRepository {
    fun summarizeArticle(articleUrl: String): Flow<String>
}
