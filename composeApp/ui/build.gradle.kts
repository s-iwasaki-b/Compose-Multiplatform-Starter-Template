plugins {
    id("kmp-compose-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.base)
            api(libs.bundles.ui)
        }
    }
}

compose.resources {
    publicResClass = true
}
