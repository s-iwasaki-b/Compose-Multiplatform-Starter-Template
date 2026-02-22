plugins {
    id("kmp-compose-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.module.ui)
            implementation(projects.module.domain.service)
        }
    }
}
