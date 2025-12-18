package io.github.kei_1111.newsflow.library.core.network.exception

sealed class GeminiException(message: String) : Exception(message) {
    data class ContentFiltered(override val message: String = "Content was filtered") : GeminiException(message)
}
