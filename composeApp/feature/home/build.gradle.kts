plugins {
    id("kmp-compose-library")
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.ui)
            implementation(projects.composeApp.domain.service)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
