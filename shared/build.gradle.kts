plugins {
    alias(libs.plugins.newsflow.library.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.shared"
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
        }

        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.domain)
            implementation(projects.core.network)

            // iOSで使用したいモジュールはapiとしてエクスポートする
            api(projects.core.model)
            api(projects.feature.home)
            api(projects.feature.viewer)

            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlin.test)
        }
    }
}

