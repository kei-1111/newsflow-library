plugins {
    alias(libs.plugins.newsflow.library.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.core.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
        }
    }
}