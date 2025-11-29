plugins {
    alias(libs.plugins.newsflow.library.kmp.library)
    alias(libs.plugins.mokkery)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.core.domain"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.model)

            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}