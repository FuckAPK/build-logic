import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * Enables Jetpack Compose and adds the dependency stack every Compose module needs.
 *
 * The library coordinates carry no version: they are pinned by the Compose BOM that comes
 * from `libs.versions.toml`.
 */
plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun composeLibrary(alias: String) = libs.findLibrary(alias).get()

pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension> {
        buildFeatures {
            compose = true
        }
    }
}

pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension> {
        buildFeatures {
            compose = true
        }
    }
}

dependencies {
    add("implementation", platform(composeLibrary("compose-bom")))
    add("implementation", composeLibrary("compose-material3"))
    add("implementation", composeLibrary("compose-ui"))
    add("implementation", composeLibrary("androidx-activity-compose"))
    add("implementation", composeLibrary("compose-ui-tooling-preview"))
    add("debugImplementation", composeLibrary("compose-ui-tooling"))
}
