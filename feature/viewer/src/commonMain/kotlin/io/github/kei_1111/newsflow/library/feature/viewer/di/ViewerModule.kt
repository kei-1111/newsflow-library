package io.github.kei_1111.newsflow.library.feature.viewer.di

import io.github.kei_1111.newsflow.library.feature.viewer.ViewerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewerModule = module {
    viewModelOf(::ViewerViewModel)
}
