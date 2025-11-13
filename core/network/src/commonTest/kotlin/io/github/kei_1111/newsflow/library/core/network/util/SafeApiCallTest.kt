package io.github.kei_1111.newsflow.library.core.network.util

import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SafeApiCallTest {

    @Test
    fun `safeApiCall returns success when API call succeeds`() = runTest {
        val result = safeApiCall {
            "Success"
        }

        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull())
    }

    @Test
    fun `safeApiCall rethrows CancellationException`() = runTest {
        assertFailsWith<CancellationException> {
            safeApiCall {
                throw CancellationException("Cancelled")
            }
        }
    }

    @Test
    fun `safeApiCall returns failure for generic exceptions`() = runTest {
        val result = safeApiCall {
            @Suppress("TooGenericExceptionThrown")
            throw RuntimeException("Something went wrong")
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun `safeApiCall returns correct data type`() = runTest {
        data class TestData(val value: String)

        val result = safeApiCall {
            TestData("test")
        }

        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull()?.value)
    }
}
