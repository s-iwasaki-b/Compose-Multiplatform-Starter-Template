plugins {
    id("kmp-compose-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.bundles.base)

            // koin-compose / koin-compose-viewmodel (in libs.bundles.base) transitively request
            // older Compose Multiplatform / androidx.lifecycle artifacts than this project's
            // catalog versions. Declare them explicitly so Gradle's version conflict resolution
            // picks the catalog (newer) versions instead of Koin's older transitive requests.
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.androidx.lifecycle.runtime.compose)
        }
    }
}
