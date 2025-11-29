package io.github.kei_1111.newsflow.library

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project

internal fun Project.configureKover(
    extension: KoverProjectExtension,
) {
    extension.apply {
        currentProject {
            sources {
                // jvmMainはKover出力のための設定のため除外
                excludedSourceSets.add("jvmMain")
            }
        }
        reports {
            total {
                xml {
                    onCheck.set(false)
                }
                html {
                    onCheck.set(false)
                }
            }
        }
    }
}