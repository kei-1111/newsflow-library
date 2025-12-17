package io.github.kei_1111.newsflow.library.core.network.core

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.defaultConfig() {
    install(ContentNegotiation) {
        json(
            json = Json { ignoreUnknownKeys = true },
            contentType = ContentType.Application.Json
        )
    }
    install(SSE)
    expectSuccess = true
}
