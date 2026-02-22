import java.io.File

const val PACKAGE_NAME = "org.starter.project"

/**
 * Deep link URI scheme.
 * NOTE: When changing this value, also update the following files:
 * - composeApp/src/commonMain/.../navigation/DeepLinkConfig.kt (SCHEME)
 * - iosApp/iosApp/Info.plist (CFBundleURLSchemes)
 */
const val DEEP_LINK_SCHEME = "cmp-starter"

fun allSubProjects(rootDir: File, action: (String) -> Unit) {
    rootDir.resolve("module").walk().maxDepth(3).filter {
        it.isDirectory && it.resolve("build.gradle.kts").exists()
    }.forEach {
        val path = it.relativeTo(rootDir).path.replace(File.separator, ":")
        action(path)
    }
}
