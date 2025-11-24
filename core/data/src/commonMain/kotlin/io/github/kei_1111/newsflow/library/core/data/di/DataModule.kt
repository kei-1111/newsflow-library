package io.github.kei_1111.newsflow.library.core.data.di

import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::NewsRepositoryImpl) bind NewsRepository::class
}
