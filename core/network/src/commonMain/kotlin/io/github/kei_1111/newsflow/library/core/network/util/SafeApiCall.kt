package io.github.kei_1111.newsflow.library.core.network.util

import io.github.kei_1111.newsflow.library.core.network.exception.NetworkException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.CancellationException

internal suspend inline fun <T> safeApiCall(
    apiCall: suspend () -> T
): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: ClientRequestException) {
        when (e.response.status) {
            HttpStatusCode.Unauthorized -> Result.failure(NetworkException.Unauthorized())
            HttpStatusCode.TooManyRequests -> Result.failure(NetworkException.RateLimitExceeded())
            HttpStatusCode.BadRequest -> Result.failure(NetworkException.BadRequest("Bad request: ${e.message}"))
            else -> Result.failure(NetworkException.BadRequest("Client error: ${e.response.status.description}"))
        }
    } catch (e: ServerResponseException) {
        Result.failure(NetworkException.ServerError("Server error: ${e.response.status.description}"))
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        Result.failure(NetworkException.NetworkFailure("Network error: ${e.message}"))
    }
}
