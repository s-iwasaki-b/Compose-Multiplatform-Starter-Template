plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.domain)

            api(projects.composeApp.base)
            implementation(projects.composeApp.data.repository)
            implementation(projects.composeApp.domain.service)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
            implementation(projects.composeApp.testing)
        }
    }
}
