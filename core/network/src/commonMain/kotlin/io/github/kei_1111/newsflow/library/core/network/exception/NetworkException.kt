package io.github.kei_1111.newsflow.library.core.network.exception

sealed class NetworkException(message: String) : Exception(message) {
    data class Unauthorized(override val message: String = "Invalid API key") : NetworkException(message)
    data class RateLimitExceeded(override val message: String = "Rate limit exceeded") : NetworkException(message)
    data class BadRequest(override val message: String) : NetworkException(message)
    data class ServerError(override val message: String) : NetworkException(message)
    data class NetworkFailure(override val message: String) : NetworkException(message)
}
