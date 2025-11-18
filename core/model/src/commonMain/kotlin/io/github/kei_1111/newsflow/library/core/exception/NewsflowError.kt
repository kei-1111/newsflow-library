package io.github.kei_1111.newsflow.library.core.exception

sealed class NewsflowError(message: String) : Exception(message) {
    data class Unauthorized(override val message: String = "Invalid API key") : NewsflowError(message)
    data class RateLimitExceeded(override val message: String = "Rate limit exceeded") : NewsflowError(message)
    data class BadRequest(override val message: String) : NewsflowError(message)
    data class ServerError(override val message: String) : NewsflowError(message)
    data class NetworkFailure(override val message: String) : NewsflowError(message)
}
