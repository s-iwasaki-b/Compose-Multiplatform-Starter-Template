plugins {
    id("kmp-compose-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(projects.composeApp.core)
            implementation(projects.composeApp.data.repository)
            implementation(projects.composeApp.data.zenn)
            implementation(projects.composeApp.domain.ai)
            implementation(projects.composeApp.domain.service)
            implementation(projects.composeApp.domain.zenn)
            implementation(projects.composeApp.feature.chat)
            implementation(projects.composeApp.feature.home)
            implementation(projects.composeApp.feature.user)
            implementation(projects.composeApp.ui)
        }
    }
}
