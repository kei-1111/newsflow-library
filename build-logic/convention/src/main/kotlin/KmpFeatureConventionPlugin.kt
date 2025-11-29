import io.github.kei_1111.newsflow.library.library
import io.github.kei_1111.newsflow.library.libs
import io.github.kei_1111.newsflow.library.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("newsflow.library.kmp.library").pluginId)
                apply(libs.plugin("mokkery").pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        api(project(":core:mvi"))
                        api(project(":core:model"))
                        implementation(project(":core:domain"))
                        implementation(project(":core:logger"))

                        implementation(libs.library("koin.core"))
                        implementation(libs.library("koin.compose.viewmodel"))
                    }
                    commonTest.dependencies {
                        implementation(project(":core:test"))
                        implementation(libs.library("koin.test"))
                        implementation(libs.library("kotlin.test"))
                        implementation(libs.library("kotlinx.coroutines.test"))
                        implementation(libs.library("turbine"))
                    }
                }
            }
        }
    }
}