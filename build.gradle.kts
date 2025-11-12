plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kmp) apply false

    // Convention Plugins
    alias(libs.plugins.newsflow.library.detekt) apply false
    alias(libs.plugins.newsflow.library.kmp.library) apply false
}