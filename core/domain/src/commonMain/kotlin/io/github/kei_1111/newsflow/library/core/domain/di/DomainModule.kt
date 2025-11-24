package io.github.kei_1111.newsflow.library.core.domain.di

import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCase
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    singleOf(::FetchTopHeadlineArticlesUseCaseImpl) bind FetchTopHeadlineArticlesUseCase::class
}
