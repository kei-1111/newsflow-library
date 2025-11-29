import io.github.kei_1111.newsflow.library.libs
import io.github.kei_1111.newsflow.library.versions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories

class MavenPublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
            }

            group = "io.github.kei-1111.newsflow.library"
            version = libs.versions("newsflowLibrary")

            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    repositories {
                        maven {
                            name = "GitHubPackages"
                            url = uri("https://maven.pkg.github.com/kei-1111/newsflow-library")
                            credentials {
                                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String?
                            }
                        }
                    }
                }
            }
        }
    }
}