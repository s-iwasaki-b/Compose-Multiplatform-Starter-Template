import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        namespace = deriveNamespace(project)
        compileSdk = libs.findVersion("android-compileSdk").get().toString().toInt()
        minSdk = libs.findVersion("android-minSdk").get().toString().toInt()

        withHostTestBuilder {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()
}
