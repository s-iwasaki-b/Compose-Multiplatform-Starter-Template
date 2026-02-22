plugins {
    id("kmp-compose-library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

fun allSubProjects(rootDir: File, action: (String) -> Unit) {
    rootDir.resolve("module").walk().maxDepth(3).filter {
        it.isDirectory && it.resolve("build.gradle.kts").exists()
    }.forEach {
        val path = it.relativeTo(rootDir).path.replace(File.separator, ":")
        action(path)
    }
}

kotlin {
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
