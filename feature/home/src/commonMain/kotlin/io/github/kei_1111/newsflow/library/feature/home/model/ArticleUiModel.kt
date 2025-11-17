package io.github.kei_1111.newsflow.library.feature.home.model

import io.github.kei_1111.newsflow.library.core.model.Article

data class ArticleUiModel(
    val id: String,
    val source: String?,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: Long,
) {
    companion object {
        fun convert(article: Article): ArticleUiModel = ArticleUiModel(
            id = article.id,
            source = article.source,
            author = article.author,
            title = article.title,
            description = article.description,
            url = article.url,
            imageUrl = article.imageUrl,
            publishedAt = article.publishedAt
        )
    }
}
