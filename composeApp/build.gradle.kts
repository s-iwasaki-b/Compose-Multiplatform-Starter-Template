plugins {
    id("kmp-compose-library")
}

fun allSubProjects(action: (String) -> Unit) {
    projectDir.walk().maxDepth(3).filter {
        it != projectDir && it.isDirectory && it.resolve("build.gradle.kts").exists()
    }.forEach {
        val path = it.relativeTo(rootDir).path.replace(File.separator, ":")
        action(path)
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            allSubProjects { api(project(":$it")) }
        }
    }
}
