import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `kotlin-dsl`
}

group = "io.github.kei_1111.newsflow.library.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.detekt.gradle)
    compileOnly(libs.kotlin.gradle)
}

gradlePlugin {
    plugins {
        register("android") {
            id = libs.plugins.newsflow.library.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("detekt") {
            id = libs.plugins.newsflow.library.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }
    }
}