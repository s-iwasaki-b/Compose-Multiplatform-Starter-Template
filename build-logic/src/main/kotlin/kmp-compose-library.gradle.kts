plugins {
    id("kmp-library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

if (project.path.startsWith(":module:")) {
    compose.resources {
        packageOfResClass = deriveNamespace(project) + ".resources"
        generateResClass = auto
    }
}
