plugins {
    alias(libs.plugins.newsflow.library.kmp.feature)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.feature.search"
    }
}