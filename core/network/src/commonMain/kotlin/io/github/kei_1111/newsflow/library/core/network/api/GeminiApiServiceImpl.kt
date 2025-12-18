package io.github.kei_1111.newsflow.library.core.network.api

import io.github.kei_1111.newsflow.library.core.network.exception.GeminiException
import io.github.kei_1111.newsflow.library.core.network.model.GeminiRequest
import io.github.kei_1111.newsflow.library.core.network.model.GeminiResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

internal class GeminiApiServiceImpl(
    private val client: HttpClient,
    private val apiKey: String,
) : GeminiApiService {

    private val json = Json { ignoreUnknownKeys = true }

    override fun summarizeUrlStream(url: String): Flow<String> = flow {
        val request = GeminiRequest.forSummarization(url)

        client.sse(
            urlString = "$BASE_URL/models/$MODEL:streamGenerateContent?alt=sse&key=$apiKey",
            request = {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(GeminiRequest.serializer(), request))
            }
        ) {
            incoming.collect { event ->
                event.data?.let { data ->
                    if (data.isNotBlank()) {
                        try {
                            val response = json.decodeFromString<GeminiResponse>(data)
                            handleResponse(response)?.let { text ->
                                emit(text)
                            }
                        } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception) {
                            // JSON parse error - skip this chunk (SSE may send partial data)
                        }
                    }
                }
            }
        }
    }

    private fun handleResponse(response: GeminiResponse): String? {
        response.error?.let { error ->
            val message = error.message ?: "Unknown error"
            throw when (error.code) {
                HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> GeminiException.InvalidApiKey(message)
                HTTP_TOO_MANY_REQUESTS -> GeminiException.QuotaExceeded(message)
                else -> GeminiException.GenerationFailed(message)
            }
        }

        val candidate = response.candidates?.firstOrNull()
        candidate?.finishReason?.let { reason ->
            if (reason == FINISH_REASON_SAFETY) {
                throw GeminiException.ContentFiltered("Content was filtered due to safety settings")
            }
        }

        return response.extractText()
    }

    private companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        const val MODEL = "gemini-2.5-flash"
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val FINISH_REASON_SAFETY = "SAFETY"
    }
}
