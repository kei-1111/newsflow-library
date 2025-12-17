package io.github.kei_1111.newsflow.library.core.network.exception

sealed class GeminiException(message: String) : Exception(message) {
    data class InvalidApiKey(override val message: String = "Invalid Gemini API key") : GeminiException(message)
    data class QuotaExceeded(override val message: String = "Gemini API quota exceeded") : GeminiException(message)
    data class ContentFiltered(override val message: String = "Content was filtered") : GeminiException(message)
    data class GenerationFailed(override val message: String) : GeminiException(message)
    data class UrlAccessFailed(override val message: String) : GeminiException(message)
}
