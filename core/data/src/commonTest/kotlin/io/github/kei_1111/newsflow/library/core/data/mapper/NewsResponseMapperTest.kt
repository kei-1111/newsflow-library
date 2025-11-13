package io.github.kei_1111.newsflow.library.core.data.mapper

import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.network.model.ArticleResponse
import io.github.kei_1111.newsflow.library.core.network.model.NewsResponse
import io.github.kei_1111.newsflow.library.core.network.model.SourceResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NewsResponseMapperTest {

    @Test
    fun `toArticles converts NewsResponse with complete data to Articles`() {
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = "test-source", name = "Test Source"),
                    author = "Test Author",
                    title = "Test Title",
                    description = "Test Description",
                    url = "https://example.com/article",
                    urlToImage = "https://example.com/image.jpg",
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = "Test Content"
                )
            )
        )

        val articles = newsResponse.toArticles()

        assertEquals(1, articles.size)
        val article = articles[0]
        assertEquals(Article.generateId("https://example.com/article"), article.id)
        assertEquals("Test Source", article.source)
        assertEquals("Test Author", article.author)
        assertEquals("Test Title", article.title)
        assertEquals("Test Description", article.description)
        assertEquals("https://example.com/article", article.url)
        assertEquals("https://example.com/image.jpg", article.imageUrl)
        assertEquals(1704067200000L, article.publishedAt) // 2024-01-01T00:00:00Z in milliseconds
    }

    @Test
    fun `toArticles converts NewsResponse with null optional fields to Articles`() {
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Test Source"),
                    author = null,
                    title = "Test Title",
                    description = null,
                    url = "https://example.com/article",
                    urlToImage = null,
                    publishedAt = "2024-01-01T12:30:00Z",
                    content = null
                )
            )
        )

        val articles = newsResponse.toArticles()

        assertEquals(1, articles.size)
        val article = articles[0]
        assertEquals(Article.generateId("https://example.com/article"), article.id)
        assertEquals("Test Source", article.source)
        assertNull(article.author)
        assertEquals("Test Title", article.title)
        assertNull(article.description)
        assertEquals("https://example.com/article", article.url)
        assertNull(article.imageUrl)
        assertEquals(1704112200000L, article.publishedAt) // 2024-01-01T12:30:00Z in milliseconds
    }

    @Test
    fun `toArticles converts NewsResponse with multiple articles`() {
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 3,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = "source-1", name = "Source 1"),
                    author = "Author 1",
                    title = "Title 1",
                    description = "Description 1",
                    url = "https://example.com/article1",
                    urlToImage = "https://example.com/image1.jpg",
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = "Content 1"
                ),
                ArticleResponse(
                    source = SourceResponse(id = "source-2", name = "Source 2"),
                    author = null,
                    title = "Title 2",
                    description = null,
                    url = "https://example.com/article2",
                    urlToImage = null,
                    publishedAt = "2024-01-02T00:00:00Z",
                    content = null
                ),
                ArticleResponse(
                    source = SourceResponse(id = null, name = "Source 3"),
                    author = "Author 3",
                    title = "Title 3",
                    description = "Description 3",
                    url = "https://example.com/article3",
                    urlToImage = "https://example.com/image3.jpg",
                    publishedAt = "2024-01-03T00:00:00Z",
                    content = "Content 3"
                )
            )
        )

        val articles = newsResponse.toArticles()

        assertEquals(3, articles.size)
        assertEquals("Title 1", articles[0].title)
        assertEquals("Title 2", articles[1].title)
        assertEquals("Title 3", articles[2].title)
    }

    @Test
    fun `toArticles converts empty NewsResponse to empty list`() {
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 0,
            articles = emptyList()
        )

        val articles = newsResponse.toArticles()

        assertEquals(0, articles.size)
    }

    @Test
    fun `toArticles generates unique IDs based on URL`() {
        val newsResponse = NewsResponse(
            status = "ok",
            totalResults = 2,
            articles = listOf(
                ArticleResponse(
                    source = SourceResponse(id = "source-1", name = "Source 1"),
                    author = "Author 1",
                    title = "Title 1",
                    description = "Description 1",
                    url = "https://example.com/article1",
                    urlToImage = "https://example.com/image1.jpg",
                    publishedAt = "2024-01-01T00:00:00Z",
                    content = "Content 1"
                ),
                ArticleResponse(
                    source = SourceResponse(id = "source-2", name = "Source 2"),
                    author = "Author 2",
                    title = "Title 2",
                    description = "Description 2",
                    url = "https://example.com/article2",
                    urlToImage = "https://example.com/image2.jpg",
                    publishedAt = "2024-01-02T00:00:00Z",
                    content = "Content 2"
                )
            )
        )

        val articles = newsResponse.toArticles()

        val id1 = Article.generateId("https://example.com/article1")
        val id2 = Article.generateId("https://example.com/article2")
        assertEquals(id1, articles[0].id)
        assertEquals(id2, articles[1].id)
    }
}