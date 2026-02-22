import org.gradle.api.Project

const val PACKAGE_NAME = "org.starter.project"

fun deriveNamespace(project: Project): String {
    if (!project.path.startsWith(":module:")) return PACKAGE_NAME
    val suffix = project.path.removePrefix(":module:").replace(":", ".")
    return "$PACKAGE_NAME.$suffix"
}
