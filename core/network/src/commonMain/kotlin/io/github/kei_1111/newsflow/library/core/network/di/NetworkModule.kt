package io.github.kei_1111.newsflow.library.core.network.di

import io.github.kei_1111.newsflow.library.core.network.api.NewsApiService
import io.github.kei_1111.newsflow.library.core.network.api.NewsApiServiceImpl
import io.github.kei_1111.newsflow.library.core.network.client.HttpClientFactory
import org.koin.dsl.module

fun networkModule(apiKey: String) = module {
    single { HttpClientFactory.create() }
    single<NewsApiService> { NewsApiServiceImpl(get(), apiKey) }
}
