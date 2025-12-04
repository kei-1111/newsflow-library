package io.github.kei_1111.newsflow.library.core.domain.di

import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCase
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchTopHeadlineArticlesUseCaseImpl
import io.github.kei_1111.newsflow.library.core.domain.usecase.GetArticleByIdUseCase
import io.github.kei_1111.newsflow.library.core.domain.usecase.GetArticleByIdUseCaseImpl
import io.github.kei_1111.newsflow.library.core.domain.usecase.SearchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.domain.usecase.SearchArticlesUseCaseImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    singleOf(::FetchTopHeadlineArticlesUseCaseImpl) bind FetchTopHeadlineArticlesUseCase::class
    singleOf(::GetArticleByIdUseCaseImpl) bind GetArticleByIdUseCase::class
    singleOf(::SearchArticlesUseCaseImpl) bind SearchArticlesUseCase::class
}
