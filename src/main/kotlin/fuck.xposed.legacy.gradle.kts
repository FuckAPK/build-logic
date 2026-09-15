import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * Marks the module as a classic Xposed module (de.robv.android.xposed, `assets/xposed_init`,
 * `xposedmodule` manifest metadata).
 */
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("compileOnly", libs.findLibrary("legacy-xposed-api").get())
}
