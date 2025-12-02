package io.github.kei_1111.newsflow.library.shared

import io.github.kei_1111.newsflow.library.core.data.di.dataModule
import io.github.kei_1111.newsflow.library.core.domain.di.domainModule
import io.github.kei_1111.newsflow.library.core.network.di.networkModule
import io.github.kei_1111.newsflow.library.feature.home.di.homeModule
import io.github.kei_1111.newsflow.library.feature.viewer.di.viewerModule
import org.koin.core.context.GlobalContext.startKoin

actual fun initKoin(newsApiKey: String, appContext: Any?) {
    startKoin {
        modules(
            networkModule(newsApiKey),
            dataModule,
            domainModule,
            homeModule,
            viewerModule,
        )
    }.koin
}
