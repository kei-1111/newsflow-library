package io.github.kei_1111.newsflow.library.core.network.util

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
            HttpStatusCode.Unauthorized ->
                Result.failure(Exception("Invalid API key"))
            HttpStatusCode.TooManyRequests ->
                Result.failure(Exception("Rate limit exceeded"))
            HttpStatusCode.BadRequest ->
                Result.failure(Exception("Bad request: ${e.message}"))
            else ->
                Result.failure(Exception("Client error: ${e.response.status.description}"))
        }
    } catch (e: ServerResponseException) {
        Result.failure(Exception("Server error: ${e.response.status.description}"))
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
}
