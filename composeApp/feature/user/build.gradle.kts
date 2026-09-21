plugins {
    id("kmp-compose-library")
    alias(libs.plugins.mokkery)
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.ui)
            implementation(projects.composeApp.domain.service)
            implementation(libs.compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}

compose.resources {
    publicResClass = false
}
