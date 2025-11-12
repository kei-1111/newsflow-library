package io.github.kei_1111.newsflow.library

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project

internal fun Project.configureDetekt(
    extension: DetektExtension,
) {
    extension.apply {
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        source.setFrom("src")
        autoCorrect = true
        parallel = true
    }
}