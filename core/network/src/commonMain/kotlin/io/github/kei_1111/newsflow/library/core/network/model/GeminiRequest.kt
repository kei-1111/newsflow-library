package io.github.kei_1111.newsflow.library.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool>,
) {
    @Serializable
    data class Content(
        val parts: List<Part>,
    )

    @Serializable
    data class Part(
        val text: String,
    )

    @Serializable
    data class Tool(
        @SerialName("url_context")
        val urlContext: UrlContext = UrlContext(),
    )

    @Serializable
    class UrlContext

    companion object {
        fun forSummarization(articleUrl: String): GeminiRequest {
            val prompt = """
                Summarize the article at the following URL in English.

                URL: $articleUrl

                Please provide a concise summary in 3-5 sentences, capturing the main points of the article.
            """.trimIndent()

            return GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                ),
                tools = listOf(Tool())
            )
        }
    }
}
