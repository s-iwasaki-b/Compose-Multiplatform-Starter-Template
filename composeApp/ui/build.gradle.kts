plugins {
    id("kmp-compose-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    androidLibrary {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material)
            api(compose.materialIconsExtended)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            api(libs.compose.components.ui.tooling.preview)
            api(libs.androidx.lifecycle.viewmodel.compose)
            api(libs.androidx.lifecycle.runtime.compose)
            api(libs.androidx.navigation.compose)

            api(projects.composeApp.base)
            api(libs.bundles.ui)
        }
    }
}

compose.resources {
    publicResClass = true
}
