plugins {
    id("kmp-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.composeApp.base)
            implementation(libs.koog.agents)
        }
    }
}
