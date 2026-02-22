plugins {
    id("kmp-compose-library")
}

kotlin {
    androidLibrary {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.module.ui)
            implementation(projects.module.domain.service)
            implementation(libs.compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = false
}
