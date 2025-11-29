package io.github.kei_1111.newsflow.library.core.network.config

object NewsflowConfig {
    internal var newsApiKey: String = ""
        private set

    fun initialize(newsApiKey: String) {
        this.newsApiKey = newsApiKey
    }
}