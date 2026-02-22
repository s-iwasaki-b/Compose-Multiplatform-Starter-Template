package org.starter.project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.starter.project.ui.route.Route
import org.starter.project.ui.route.Router

class AppRouter(
    internal val navController: NavHostController,
) : Router {
    override fun navigate(route: Route) {
        navController.navigate(route)
    }

    override fun popBackStack() {
        navController.popBackStack()
    }

    fun handleDeepLink(uri: String) {
        val route = parseDeepLinkUri(uri) ?: return
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
}

internal fun parseDeepLinkUri(uri: String): Route? {
    val schemeDelimiter = "://"
    val schemeEndIndex = uri.indexOf(schemeDelimiter)
    if (schemeEndIndex == -1) return null

    val scheme = uri.substring(0, schemeEndIndex)
    if (scheme != DeepLinkConfig.SCHEME) return null

    val withoutScheme = uri.substring(schemeEndIndex + schemeDelimiter.length)
    val pathAndQuery = withoutScheme.split("?", limit = 2)
    val path = pathAndQuery[0].trimEnd('/')
    val queryString = pathAndQuery.getOrNull(1)

    val queryParams = queryString?.split("&")?.associate { param ->
        val parts = param.split("=", limit = 2)
        parts[0] to (parts.getOrNull(1) ?: "")
    } ?: emptyMap()

    return when {
        path == "home" -> Route.Home(keyword = queryParams["keyword"])
        path.startsWith("user/") -> {
            val username = path.removePrefix("user/")
            if (username.isNotEmpty()) Route.User(username = username) else null
        }
        else -> null
    }
}

@Composable
fun rememberAppRouter(
    navController: NavHostController = rememberNavController()
) = remember(navController) {
    AppRouter(navController)
}
