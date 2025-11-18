package io.github.kei_1111.newsflow.library.core.domain.di

import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCaseImpl
import org.koin.dsl.module

val domainModule = module {
    single<FetchArticlesUseCase> { FetchArticlesUseCaseImpl(get()) }
}
