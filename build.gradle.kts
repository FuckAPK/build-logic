plugins {
    `kotlin-dsl`
}

group = "org.lyaaz.buildlogic"

/**
 * The Android Gradle Plugin, the Kotlin Gradle Plugin and the Compose compiler plugin are
 * put on this build's classpath so that every module of every FuckAPK project only has to
 * write `id("fuck.…")` without repeating a version.
 *
 * `gradle/libs.versions.toml` in this repository is the single source of truth for these
 * versions; the consumer repositories read the very same file (see README.md).
 */
dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.compose.compiler.plugin)
}
