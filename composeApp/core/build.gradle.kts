plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        commonMain.dependencies {
            api(projects.composeApp.base)
            api(libs.bundles.core)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
