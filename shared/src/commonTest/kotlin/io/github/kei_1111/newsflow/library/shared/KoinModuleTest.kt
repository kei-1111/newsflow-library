package io.github.kei_1111.newsflow.library.shared

import io.github.kei_1111.newsflow.library.core.data.di.dataModule
import io.github.kei_1111.newsflow.library.core.data.repository.NewsRepository
import io.github.kei_1111.newsflow.library.core.domain.di.domainModule
import io.github.kei_1111.newsflow.library.core.domain.usecase.FetchArticlesUseCase
import io.github.kei_1111.newsflow.library.core.network.api.NewsApiService
import io.github.kei_1111.newsflow.library.core.network.di.networkModule
import io.github.kei_1111.newsflow.library.feature.home.HomeViewModel
import io.github.kei_1111.newsflow.library.feature.home.di.homeModule
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class KoinModuleTest : KoinTest {

    @BeforeTest
    fun setup() {
        startKoin {
            modules(
                networkModule,
                dataModule,
                domainModule,
                homeModule,
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify HttpClient can be resolved`() {
        val httpClient = get<HttpClient>()
        assertNotNull(httpClient)
    }

    @Test
    fun `verify NewsApiService can be resolved`() {
        val newsApiService = get<NewsApiService>()
        assertNotNull(newsApiService)
    }

    @Test
    fun `verify NewsRepository can be resolved`() {
        val newsRepository = get<NewsRepository>()
        assertNotNull(newsRepository)
    }

    @Test
    fun `verify FetchArticlesUseCase can be resolved`() {
        val fetchArticlesUseCase = get<FetchArticlesUseCase>()
        assertNotNull(fetchArticlesUseCase)
    }

    @Test
    fun `verify HomeViewModel can be resolved`() {
        val homeViewModel = get<HomeViewModel>()
        assertNotNull(homeViewModel)
    }
}
