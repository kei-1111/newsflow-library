package io.github.kei_1111.newsflow.library.core.model

data class Article(
    val id: String,
    val source: String?,
    val author: String?,
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    val publishedAt: Long,
) {
    companion object {
        fun generateId(url: String) = url.hashCode().toString()
    }
}
