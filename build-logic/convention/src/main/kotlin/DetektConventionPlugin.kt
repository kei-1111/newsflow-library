import io.github.kei_1111.newsflow.library.configureDetekt
import io.github.kei_1111.newsflow.library.detektPlugins
import io.github.kei_1111.newsflow.library.library
import io.github.kei_1111.newsflow.library.libs
import io.github.kei_1111.newsflow.library.plugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("detekt").pluginId)
            }

            dependencies {
                detektPlugins(libs.library("detekt.formatting"))
            }

            configureDetekt(extensions.getByType<DetektExtension>())
        }
    }
}