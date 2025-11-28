package io.github.kei_1111.newsflow.library

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project

internal fun Project.configureKover(
    extension: KoverProjectExtension,
) {
    extension.apply {
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