plugins {
    id("kmp-library")
    alias(libs.plugins.mokkery)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.base)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
