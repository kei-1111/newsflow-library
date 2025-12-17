package io.github.kei_1111.newsflow.library.core.network.api

import kotlinx.coroutines.flow.Flow

interface GeminiApiService {
    fun summarizeUrlStream(url: String): Flow<String>
}
