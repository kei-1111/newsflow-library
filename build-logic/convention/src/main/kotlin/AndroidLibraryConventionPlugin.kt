import com.android.build.gradle.LibraryExtension
import io.github.kei_1111.newsflow.library.libs
import io.github.kei_1111.newsflow.library.plugin
import io.github.kei_1111.newsflow.library.versions
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("android.library").pluginId)
            }

            extensions.configure<LibraryExtension> {
                compileSdk = libs.versions("android-compileSdk").toInt()
                defaultConfig.minSdk = libs.versions("android-minSdk").toInt()

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
            }

            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }
}