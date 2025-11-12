import com.android.build.api.dsl.androidLibrary
import io.github.kei_1111.newsflow.library.libs
import io.github.kei_1111.newsflow.library.plugin
import io.github.kei_1111.newsflow.library.versions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Locale

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugin("android.kmp.library").pluginId)
                apply(libs.plugin("kmp").pluginId)
                apply(libs.plugin("newsflow.library.detekt").pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidLibrary {
                    compileSdk = libs.versions("android-compileSdk").toInt()
                    minSdk = libs.versions("android-minSdk").toInt()

                    compilations.configureEach {
                        compilerOptions.configure {
                            jvmTarget.set(JvmTarget.JVM_21)
                        }
                    }
                }

                val frameworkBaseName = project.name.replaceFirstChar { it.uppercase() }
                val xcf = XCFramework(frameworkBaseName)
                listOf(
                    iosX64(),
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = frameworkBaseName
                        isStatic = true
                        xcf.add(this)
                    }
                }
            }
        }
    }
}