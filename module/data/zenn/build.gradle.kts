import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.mokkery)
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}

kotlin {
    androidLibrary {
        namespace = "$PACKAGE_NAME.data.zenn"
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
            implementation(libs.bundles.data)

            api(projects.module.base)
            implementation(projects.module.core)
            implementation(projects.module.data.repository)
        }
        commonTest.dependencies {
            implementation(libs.bundles.test)
        }
    }
}
