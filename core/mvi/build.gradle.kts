plugins {
    alias(libs.plugins.newsflow.library.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.core.mvi"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel)
        }
    }
}
