import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `kotlin-dsl`
}

group = "io.github.kei_1111.newsflow.library.buildlogic"

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.detekt.gradle)
    compileOnly(libs.kotlin.gradle)
    compileOnly(libs.kover.gradle)
}

gradlePlugin {
    plugins {
        register("detekt") {
            id = libs.plugins.newsflow.library.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }
        register("kmpLibrary") {
            id = libs.plugins.newsflow.library.kmp.library.get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpFeature") {
            id = libs.plugins.newsflow.library.kmp.feature.get().pluginId
            implementationClass = "KmpFeatureConventionPlugin"
        }
        register("kover") {
            id = libs.plugins.newsflow.library.kover.get().pluginId
            implementationClass = "KoverConventionPlugin"
        }
        register("mavenPublish") {
            id = libs.plugins.newsflow.library.maven.publish.get().pluginId
            implementationClass = "MavenPublishConventionPlugin"
        }
    }
}