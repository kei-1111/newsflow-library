package io.github.kei_1111.newsflow.library.core.data.repository

import io.github.kei_1111.newsflow.library.core.data.mapper.toArticles
import io.github.kei_1111.newsflow.library.core.data.util.toNewsflowError
import io.github.kei_1111.newsflow.library.core.model.Article
import io.github.kei_1111.newsflow.library.core.model.NewsflowError
import io.github.kei_1111.newsflow.library.core.network.api.NewsApiService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class NewsRepositoryImpl(
    private val newsApiService: NewsApiService,
) : NewsRepository {
    private val cache = mutableMapOf<String, List<Article>>()
    private val searchCache = mutableMapOf<String, List<Article>>()
    private val cacheMutex = Mutex()

    override suspend fun fetchArticles(
        category: String,
        forceRefresh: Boolean,
    ): Result<List<Article>> = cacheMutex.withLock {
        if (!forceRefresh) {
            cache[category]?.let { return@withLock Result.success(it) }
        }

        return@withLock newsApiService.fetchTopHeadlines(category).fold(
            onSuccess = { response ->
                val articles = response.toArticles()
                cache[category] = articles
                Result.success(articles)
            },
            onFailure = { error ->
                cache.remove(category)
                Result.failure(error.toNewsflowError())
            },
        )
    }

    override suspend fun getArticleById(id: String): Result<Article> = cacheMutex.withLock {
        val article = cache.values.flatten().firstOrNull { it.id == id }
            ?: searchCache.values.flatten().firstOrNull { it.id == id }
        article?.let { Result.success(it) }
            ?: Result.failure(NewsflowError.InternalError.ArticleNotFound("Article with id $id not found"))
    }

    override suspend fun searchArticles(query: String): Result<List<Article>> = cacheMutex.withLock {
        return@withLock newsApiService.searchArticles(query).fold(
            onSuccess = { response ->
                val articles = response.toArticles()
                searchCache[query] = articles
                Result.success(articles)
            },
            onFailure = { error ->
                Result.failure(error.toNewsflowError())
            },
        )
    }
}
