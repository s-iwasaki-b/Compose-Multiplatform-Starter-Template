import org.gradle.api.Project

fun deriveNamespace(project: Project): String {
    val packageName = project.property("packageName").toString()
    return packageName + "." + project.path.removePrefix(":module:").replace(":", ".")
}
