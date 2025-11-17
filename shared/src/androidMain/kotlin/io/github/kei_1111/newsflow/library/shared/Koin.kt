package io.github.kei_1111.newsflow.library.shared

import android.content.Context
import io.github.kei_1111.newsflow.library.core.data.di.dataModule
import io.github.kei_1111.newsflow.library.core.network.di.networkModule
import io.github.kei_1111.newsflow.library.feature.home.di.homeModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

actual fun initKoin(appContext: Any?) {
    startKoin {
        appContext?.let { androidContext(it as Context) }
        modules(networkModule, dataModule, homeModule)
    }.koin
}