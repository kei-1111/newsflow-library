package io.github.kei_1111.newsflow.library.core.data.mapper

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.network.model.ArticleResponse
import io.github.kei_1111.newsflow.library.core.network.model.NewsResponse
import kotlinx.datetime.Instant

internal fun NewsResponse.toArticles(): List<Article> {
    return articles.map { it.toArticle() }
}

private fun ArticleResponse.toArticle(): Article {
    return Article(
        id = Article.generateId(url),
        source = source.name,
        author = author,
        title = title,
        description = description,
        url = url,
        imageUrl = urlToImage,
        publishedAt = Instant.parse(publishedAt).toEpochMilliseconds(),
    )
}