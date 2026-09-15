import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Shared configuration for every FuckAPK *application* module.
 *
 * Provided defaults (override them in the module's own `android { }` block, which always runs
 * after this plugin):
 *
 *  - compileSdk 36 / targetSdk 36 / minSdk 26
 *  - Java 21 + Kotlin jvmTarget 21
 *  - `versionCode` / `versionName` derived from the git history, with fallbacks
 *  - release build type: R8 + resource shrinking + release signing when available
 *  - debug build type: `.debug` application id suffix
 *  - `buildConfig = true`, `vectorDrawables.useSupportLibrary = true`
 *  - `localeFilters` starts at `en`
 *
 * Release signing is only wired up when `signing.properties` exists in the repository root.
 * A missing file (fresh clone, CI verification run, Dependabot) therefore no longer breaks
 * the whole build: `assembleRelease` simply produces an unsigned APK.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/** Runs `git` in the repository root and returns its trimmed stdout ("" when it fails). */
fun gitOutput(vararg args: String) = providers.exec {
    commandLine("git", *args)
    isIgnoreExitValue = true
    workingDir = rootProject.projectDir
}.standardOutput.asText.map { it.trim() }

val computedVersionCode = gitOutput("rev-list", "HEAD", "--count")
    .map { it.toIntOrNull() ?: 1 }
    .orElse(1)

val computedVersionName = gitOutput("describe", "--tag", "--always")
    .map { it.removePrefix("v").ifEmpty { "0.0.0" } }
    .orElse("0.0.0")

val signingPropertiesFile = rootProject.file("signing.properties")
val signingProperties = Properties()
val hasReleaseSigning = signingPropertiesFile.isFile
if (hasReleaseSigning) {
    signingPropertiesFile.inputStream().use(signingProperties::load)
} else {
    logger.lifecycle(
        "fuck: ${rootProject.name}: signing.properties not found - " +
            "release builds produce an unsigned APK"
    )
}

extensions.configure<ApplicationExtension> {
    compileSdk = 36

    defaultConfig {
        targetSdk = 36
        minSdk = 26
        versionCode = computedVersionCode.get()
        versionName = computedVersionName.get()
        vectorDrawables.useSupportLibrary = true
    }

    androidResources {
        localeFilters.add("en")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    val releaseSigningConfig = if (hasReleaseSigning) {
        signingConfigs.create("release") {
            storeFile = rootProject.file(signingProperties.getProperty("storeFilePath"))
            storePassword = signingProperties.getProperty("storePassword")
            keyPassword = signingProperties.getProperty("keyPassword")
            keyAlias = signingProperties.getProperty("keyAlias")
        }
    } else {
        null
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (releaseSigningConfig != null) {
                signingConfig = releaseSigningConfig
            }
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }
}

extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
