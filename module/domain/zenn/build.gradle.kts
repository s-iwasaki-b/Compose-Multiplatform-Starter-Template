import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.mokkery)
}

kotlin {
    androidLibrary {
        namespace = "$PACKAGE_NAME.domain.zenn"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.domain)

            api(projects.module.base)
            implementation(projects.module.data.repository)
            implementation(projects.module.domain.service)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
