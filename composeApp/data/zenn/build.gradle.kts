plugins {
    id("kmp-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktorfit)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.data)

            api(projects.composeApp.base)
            implementation(projects.composeApp.core)
            implementation(projects.composeApp.data.repository)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
