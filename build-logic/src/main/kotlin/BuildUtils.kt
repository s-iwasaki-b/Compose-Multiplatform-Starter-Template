import org.gradle.api.Project

const val PACKAGE_NAME = "org.starter.project"

/**
 * Deep link URI scheme.
 * NOTE: When changing this value, also update the following files:
 * - composeApp/src/commonMain/.../navigation/DeepLinkConfig.kt (SCHEME)
 * - iosApp/iosApp/Info.plist (CFBundleURLSchemes)
 */
const val DEEP_LINK_SCHEME = "cmp-starter"

fun deriveNamespace(project: Project): String {
    if (!project.path.startsWith(":composeApp:")) return PACKAGE_NAME
    val suffix = project.path.removePrefix(":composeApp:").replace(":", ".")
    return "$PACKAGE_NAME.$suffix"
}
