package io.github.kei_1111.newsflow.library.core.model

import io.github.kei_1111.newsflow.library.core.exception.NewsflowError

sealed interface NewsflowErrorType {
    data object Unauthorized : NewsflowErrorType
    data object RateLimitExceeded : NewsflowErrorType
    data object BadRequest : NewsflowErrorType
    data object ServerError : NewsflowErrorType
    data object NetworkFailure : NewsflowErrorType
    data object Unknown : NewsflowErrorType
}

fun NewsflowError.toType(): NewsflowErrorType = when(this) {
    is NewsflowError.Unauthorized -> NewsflowErrorType.Unauthorized
    is NewsflowError.RateLimitExceeded -> NewsflowErrorType.RateLimitExceeded
    is NewsflowError.BadRequest -> NewsflowErrorType.BadRequest
    is NewsflowError.ServerError -> NewsflowErrorType.ServerError
    is NewsflowError.NetworkFailure -> NewsflowErrorType.NetworkFailure
}
