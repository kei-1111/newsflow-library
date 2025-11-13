import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.build.konfig)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.newsflow.library.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "io.github.kei_1111.newsflow.library.core.network"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

buildkonfig {
    packageName = "io.github.kei_1111.newsflow.library.core.network"

    defaultConfigs {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }

        val apiKey = localProperties.getProperty("NEWS_API_KEY") ?: ""
        buildConfigField(FieldSpec.Type.STRING, "NEWS_API_KEY", apiKey)
    }
}