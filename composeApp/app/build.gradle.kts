plugins {
    id("kmp-compose-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(projects.composeApp.core)
            implementation(projects.composeApp.data.repository)
            implementation(projects.composeApp.data.zenn)
            implementation(projects.composeApp.domain.service)
            implementation(projects.composeApp.domain.zenn)
            implementation(projects.composeApp.feature.home)
            implementation(projects.composeApp.feature.user)
            implementation(projects.composeApp.ui)
        }
    }
}
