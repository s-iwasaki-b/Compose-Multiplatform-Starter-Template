plugins {
    id("kmp-compose-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.bundles.base)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.androidx.lifecycle.runtime.compose)
        }
    }
}
