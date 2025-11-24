package io.github.kei_1111.newsflow.library.core.test.model

import io.github.kei_1111.newsflow.library.core.model.Article

fun createTestArticle(index: Int, prefix: String = "Test") = Article(
    id = "$index",
    source = "$prefix Source $index",
    author = "$prefix Author $index",
    title = "$prefix Title $index",
    description = "$prefix Description $index",
    url = "https://example.com/$prefix-$index",
    imageUrl = "https://example.com/image-$index.jpg",
    publishedAt = 1234567890000L + index,
)

fun createTestArticles(count: Int, prefix: String = "Test") =
    List(count) { createTestArticle(it + 1, prefix) }