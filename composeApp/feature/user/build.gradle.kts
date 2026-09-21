plugins {
    id("kmp-compose-library")
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
    }
}

compose.resources {
    publicResClass = false
}
