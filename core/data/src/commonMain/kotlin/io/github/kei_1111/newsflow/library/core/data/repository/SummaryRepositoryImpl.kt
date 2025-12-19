package io.github.kei_1111.newsflow.library.core.data.repository

import io.github.kei_1111.newsflow.library.core.data.util.toNewsflowError
import io.github.kei_1111.newsflow.library.core.network.api.GeminiApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SummaryRepositoryImpl(
    private val geminiApiService: GeminiApiService,
) : SummaryRepository {
    private val cache = mutableMapOf<String, String>()
    private val cacheMutex = Mutex()

    override fun summarizeArticle(articleUrl: String): Flow<String> = flow {
        val cachedSummary = cacheMutex.withLock { cache[articleUrl] }
        if (cachedSummary != null) {
            emit(cachedSummary)
            return@flow
        }

        val summaryBuilder = StringBuilder()
        geminiApiService.summarizeUrlStream(articleUrl)
            .onCompletion { cause ->
                if (cause == null && summaryBuilder.isNotEmpty()) {
                    cacheMutex.withLock {
                        cache[articleUrl] = summaryBuilder.toString()
                    }
                }
            }
            .catch { e ->
                throw e.toNewsflowError()
            }
            .collect { text ->
                summaryBuilder.append(text)
                emit(text)
            }
    }
}
