plugins {
    id("kmp-compose-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.ui)
            implementation(projects.composeApp.domain.service)
        }
    }
}
