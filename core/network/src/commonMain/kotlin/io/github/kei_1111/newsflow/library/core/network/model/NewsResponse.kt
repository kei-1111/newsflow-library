package io.github.kei_1111.newsflow.library.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleResponse>,
    val code: String? = null,
    val message: String? = null
)

@Serializable
data class ArticleResponse(
    val source: SourceResponse,
    val author: String? = null,
    val title: String,
    val description: String? = null,
    val url: String,
    val urlToImage: String? = null,
    val publishedAt: String,
    val content: String? = null
)

@Serializable
data class SourceResponse(
    val id: String? = null,
    val name: String
)
