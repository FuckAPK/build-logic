import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * Marks the module as a modern libxposed module (io.github.libxposed,
 * `META-INF/xposed/{module.prop,java_init.list,scope.list}`).
 *
 * The `META-INF/xposed/*` entries are merged instead of being dropped when several libxposed
 * artifacts end up on the packaging classpath.
 */
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("compileOnly", libs.findLibrary("libxposed-api").get())
    add("implementation", libs.findLibrary("libxposed-service").get())
}

pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension> {
        packaging {
            resources {
                merges += "META-INF/xposed/*"
            }
        }
    }
}
