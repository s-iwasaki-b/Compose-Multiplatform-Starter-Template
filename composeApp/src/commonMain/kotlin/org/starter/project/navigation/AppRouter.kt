package org.starter.project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter

internal class AppRouterImpl(
    internal val navController: NavHostController,
) : AppRouter {
    override fun navigate(route: AppRoute) {
        navController.navigate(route)
    }

    override fun popBackStack() {
        navController.popBackStack()
    }
}

@Composable
internal fun rememberAppRouter(
    navController: NavHostController = rememberNavController()
) = remember(navController) {
    AppRouterImpl(navController)
}
