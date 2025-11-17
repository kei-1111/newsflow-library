import io.github.kei_1111.newsflow.library.libs
import io.github.kei_1111.newsflow.library.versions
import org.gradle.api.Plugin
import org.gradle.api.Project

class MavenPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
            }

            group = "io.github.kei-1111.newsflow.library"
            version = libs.versions("newsflowLibrary")
        }
    }
}