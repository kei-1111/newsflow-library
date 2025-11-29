plugins {
    alias(libs.plugins.newsflow.library.kmp.library)
    alias(libs.plugins.mokkery)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.core.data"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
            implementation(projects.core.network)

            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(projects.core.test)

            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}