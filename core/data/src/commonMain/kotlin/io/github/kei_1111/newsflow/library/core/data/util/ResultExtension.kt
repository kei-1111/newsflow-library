package io.github.kei_1111.newsflow.library.core.data.util

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.network.exception.GeminiException
import io.github.kei_1111.newsflow.library.core.network.exception.NetworkException

internal fun Throwable.toNewsflowError(): NewsflowError = when (this) {
    is NetworkException.Unauthorized -> NewsflowError.NetworkError.Unauthorized(message)
    is NetworkException.RateLimitExceeded -> NewsflowError.NetworkError.RateLimitExceeded(message)
    is NetworkException.BadRequest -> NewsflowError.NetworkError.BadRequest(message)
    is NetworkException.ServerError -> NewsflowError.NetworkError.ServerError(message)
    is NetworkException.NetworkFailure -> NewsflowError.NetworkError.NetworkFailure(message)
    is GeminiException.ContentFiltered -> NewsflowError.NetworkError.ContentFiltered(message)
    else -> NewsflowError.NetworkError.NetworkFailure(message ?: "Unknown error")
}
