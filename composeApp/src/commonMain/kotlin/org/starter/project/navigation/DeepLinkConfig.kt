package org.starter.project.navigation

/**
 * Deep link configuration.
 * NOTE: When changing [SCHEME], also update the following files:
 * - buildSrc/src/main/kotlin/BuildUtils.kt (DEEP_LINK_SCHEME)
 * - iosApp/iosApp/Info.plist (CFBundleURLSchemes)
 */
object DeepLinkConfig {
    const val SCHEME = "cmp-starter"
}
