# build-logic

Shared Gradle convention plugins for every FuckAPK project (the ten modules plus `ui`).

The point of this repository is that the Android/Kotlin/Compose configuration, the signing
setup and the dependency versions exist **once**. A project module file should only describe
what makes it different.

## Convention plugins

| plugin id | applies to | provides |
|---|---|---|
| `fuck.android.application` | `:app` of an application project | AGP application + Kotlin, compileSdk 36 / targetSdk 36 / minSdk 26, Java 21, git-based `versionCode`/`versionName`, release build type (R8 + shrink + signing when available), debug `.debug` suffix, `buildConfig`, `vectorDrawables.useSupportLibrary`, `localeFilters = [en]` |
| `fuck.android.library` | library modules such as `:ui` | AGP library + Kotlin, compileSdk 36, minSdk 24, Java 21, `consumer-rules.pro`, non-minified release |
| `fuck.compose` | any module using Compose | Compose compiler plugin, `buildFeatures.compose = true` and the whole Compose dependency stack pinned by the Compose BOM |
| `fuck.xposed.legacy` | classic Xposed modules | `compileOnly(de.robv.android.xposed:api)` |
| `fuck.xposed.modern` | libxposed modules | `compileOnly(io.github.libxposed:api)`, `implementation(io.github.libxposed:service)`, `META-INF/xposed/*` merge rule |

Anything a plugin sets can be overridden in the module's own `android { }` block, which always
runs after the plugin.

## How a project consumes it

`build-logic` is a git submodule at `<project>/build-logic`. The project's `settings.gradle.kts`
wires it up and imports the version catalog from it:

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() /* + project specific repositories */ }
    versionCatalogs {
        create("libs") { from(files("build-logic/gradle/libs.versions.toml")) }
    }
}
```

A module then looks like this:

```kotlin
plugins {
    id("fuck.android.application")
    id("fuck.compose")
    id("fuck.xposed.modern")
}

android {
    namespace = "org.lyaaz.fuckclip"
    defaultConfig { minSdk = 34 }
}

dependencies {
    implementation(libs.material)
}
```

## Versions

`gradle/libs.versions.toml` is the single source of truth for AGP, Kotlin, the Compose compiler
plugin, the Compose BOM and every library the projects depend on. Bump it here and all projects
follow on their next build.

Because the Kotlin Gradle Plugin and `org.jetbrains.kotlin.plugin.compose` are both declared
here, they can no longer drift apart - the failure mode where Dependabot bumped only
`org.jetbrains.kotlin.android` and left the Compose compiler behind is gone.

## Signing

`fuck.android.application` reads `<repository-root>/signing.properties`. When the file is
missing the build still succeeds and `assembleRelease` produces an **unsigned** APK, which is
what lets CI verify every pull request without access to the release secrets.

## Local verification

```bash
# from a project repository, after `git submodule update --init --recursive`
./gradlew assembleDebug
```
