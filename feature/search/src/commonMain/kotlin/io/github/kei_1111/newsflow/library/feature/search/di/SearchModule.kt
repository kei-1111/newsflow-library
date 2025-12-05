package io.github.kei_1111.newsflow.library.feature.search.di

import io.github.kei_1111.newsflow.library.feature.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchModule = module {
    viewModelOf(::SearchViewModel)
}
