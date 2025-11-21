package io.github.kei_1111.newsflow.library.core.data.util

import io.github.kei_1111.newsflow.library.core.exception.NewsflowError
import io.github.kei_1111.newsflow.library.core.network.exception.NetworkException

internal fun Throwable.toNewsflowError(): NewsflowError = when (this) {
    is NetworkException.Unauthorized -> NewsflowError.Unauthorized(message)
    is NetworkException.RateLimitExceeded -> NewsflowError.RateLimitExceeded(message)
    is NetworkException.BadRequest -> NewsflowError.BadRequest(message)
    is NetworkException.ServerError -> NewsflowError.ServerError(message)
    is NetworkException.NetworkFailure -> NewsflowError.NetworkFailure(message)
    else -> NewsflowError.NetworkFailure(message ?: "Unknown error")
}
