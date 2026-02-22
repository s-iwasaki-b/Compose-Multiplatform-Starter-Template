import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val packageName: String by project

fun allSubProjects(rootDir: File, action: (String) -> Unit) {
    rootDir.resolve("module").walk().maxDepth(3).filter {
        it.isDirectory && it.resolve("build.gradle.kts").exists()
    }.forEach {
        val path = it.relativeTo(rootDir).path.replace(File.separator, ":")
        action(path)
    }
}

kotlin {
    androidLibrary {
        namespace = packageName
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            allSubProjects(rootDir) { export(project(":$it")) }

            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            allSubProjects(rootDir) { api(project(":$it")) }
        }
    }
}
