package io.github.kei_1111.newsflow.library.core.data.util

import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.network.exception.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ResultExtensionTest {

    @Test
    fun `toNewsflowError converts Unauthorized to NewsflowError Unauthorized`() {
        val exception = NetworkException.Unauthorized("Invalid API key")

        val result = exception.toNewsflowError()

        assertIs<NewsflowError.Unauthorized>(result)
        assertEquals("Invalid API key", result.message)
    }

    @Test
    fun `toNewsflowError converts RateLimitExceeded to NewsflowError RateLimitExceeded`() {
        val exception = NetworkException.RateLimitExceeded("Rate limit exceeded")

        val result = exception.toNewsflowError()

        assertIs<NewsflowError.RateLimitExceeded>(result)
        assertEquals("Rate limit exceeded", result.message)
    }

    @Test
    fun `toNewsflowError converts BadRequest to NewsflowError BadRequest`() {
        val exception = NetworkException.BadRequest("Bad request")

        val result = exception.toNewsflowError()

        assertIs<NewsflowError.BadRequest>(result)
        assertEquals("Bad request", result.message)
    }

    @Test
    fun `toNewsflowError converts ServerError to NewsflowError ServerError`() {
        val exception = NetworkException.ServerError("Internal server error")

        val result = exception.toNewsflowError()

        assertIs<NewsflowError.ServerError>(result)
        assertEquals("Internal server error", result.message)
    }

    @Test
    fun `toNewsflowError converts NetworkFailure to NewsflowError NetworkFailure`() {
        val exception = NetworkException.NetworkFailure("Network error")

        val result = exception.toNewsflowError()

        assertIs<NewsflowError.NetworkFailure>(result)
        assertEquals("Network error", result.message)
    }

    @Test
    fun `toNewsflowError converts unknown exception to NewsflowError NetworkFailure`() {
        val exception = RuntimeException("Unknown error")

        val result = exception.toNewsflowError()

        assertIs<NewsflowError.NetworkFailure>(result)
        assertEquals("Unknown error", result.message)
    }

    @Test
    fun `toNewsflowError uses default message when exception message is null`() {
        val exception = RuntimeException(null as String?)

        val result = exception.toNewsflowError()

        assertIs<NewsflowError.NetworkFailure>(result)
        assertEquals("Unknown error", result.message)
    }
}