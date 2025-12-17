package io.github.kei_1111.newsflow.library.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: ErrorResponse? = null,
) {
    @Serializable
    data class Candidate(
        val content: Content? = null,
        val finishReason: String? = null,
    )

    @Serializable
    data class Content(
        val parts: List<Part>? = null,
        val role: String? = null,
    )

    @Serializable
    data class Part(
        val text: String? = null,
    )

    @Serializable
    data class ErrorResponse(
        val code: Int? = null,
        val message: String? = null,
        val status: String? = null,
    )

    fun extractText(): String? {
        return candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }
}
