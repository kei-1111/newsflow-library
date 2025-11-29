import io.github.kei_1111.newsflow.library.configureKover
import io.github.kei_1111.newsflow.library.libs
import io.github.kei_1111.newsflow.library.plugin
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("kover").pluginId)
            }

            configureKover(extensions.getByType<KoverProjectExtension>())
        }
    }
}