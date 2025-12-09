package io.github.kei_1111.newsflow.library

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

internal fun Project.configureDetekt(
    extension: DetektExtension,
) {
    extension.apply {
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        source.setFrom("src")
        autoCorrect = true
        parallel = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = JavaVersion.VERSION_21.toString()
        outputs.cacheIf { true }
        reports {
            html.required.set(true)
            xml.required.set(true)
        }
    }
}