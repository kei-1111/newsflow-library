package io.github.kei_1111.newsflow.library.core.network.api

import io.github.kei_1111.newsflow.library.core.network.model.NewsResponse
import io.github.kei_1111.newsflow.library.core.network.util.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

internal class NewsApiServiceImpl(
    private val client: HttpClient,
    private val apiKey: String,
) : NewsApiService {
    override suspend fun fetchTopHeadlines(
        category: String
    ): Result<NewsResponse> = safeApiCall {
        client.get(BASE_URL + TOP_HEADLINES) {
            header("X-Api-Key", apiKey)
            parameter("category", category)
            parameter("country", COUNTRY)
        }.body()
    }

    override suspend fun searchArticles(
        query: String
    ): Result<NewsResponse> = safeApiCall {
        client.get(BASE_URL + EVERYTHING) {
            header("X-Api-Key", apiKey)
            parameter("q", query)
            parameter("sortBy", SORT_BY_RELEVANCY)
        }.body()
    }

    private companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val TOP_HEADLINES = "top-headlines"
        const val EVERYTHING = "everything"
        const val COUNTRY = "us"
        const val SORT_BY_RELEVANCY = "relevancy"
    }
}
