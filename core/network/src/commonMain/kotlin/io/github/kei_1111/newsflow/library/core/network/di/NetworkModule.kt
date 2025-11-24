package io.github.kei_1111.newsflow.library.core.network.di

import io.github.kei_1111.newsflow.library.core.network.api.NewsApiService
import io.github.kei_1111.newsflow.library.core.network.api.NewsApiServiceImpl
import io.github.kei_1111.newsflow.library.core.network.client.HttpClientFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create() }
    singleOf(::NewsApiServiceImpl) bind NewsApiService::class
}
