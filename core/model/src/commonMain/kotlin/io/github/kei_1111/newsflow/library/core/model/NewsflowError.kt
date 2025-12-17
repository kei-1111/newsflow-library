package io.github.kei_1111.newsflow.library.core.model

sealed class NewsflowError(message: String) : Exception(message) {
    sealed class NetworkError(message: String) : NewsflowError(message) {
        data class Unauthorized(override val message: String = "Invalid API key") : NetworkError(message)
        data class RateLimitExceeded(override val message: String = "Rate limit exceeded") : NetworkError(message)
        data class BadRequest(override val message: String) : NetworkError(message)
        data class ServerError(override val message: String) : NetworkError(message)
        data class NetworkFailure(override val message: String) : NetworkError(message)
    }

    sealed class InternalError(message: String) : NewsflowError(message) {
        data class ArticleNotFound(override val message: String = "Article not found") : InternalError(message)
        data class InvalidParameter(override val message: String) : InternalError(message)
    }

    sealed class AIError(message: String) : NewsflowError(message) {
        data class InvalidApiKey(override val message: String = "Invalid Gemini API key") : AIError(message)
        data class QuotaExceeded(override val message: String = "Gemini API quota exceeded") : AIError(message)
        data class ContentFiltered(override val message: String = "Content was filtered") : AIError(message)
        data class GenerationFailed(override val message: String) : AIError(message)
        data class UrlAccessFailed(override val message: String) : AIError(message)
    }
}
