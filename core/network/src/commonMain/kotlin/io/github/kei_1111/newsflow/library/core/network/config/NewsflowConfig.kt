package io.github.kei_1111.newsflow.library.core.network.config

object NewsflowConfig {
    internal var apiKey: String = ""
        private set

    fun initialize(apiKey: String) {
        this.apiKey = apiKey
    }
}