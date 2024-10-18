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
}

@Composable
fun rememberAppRouter(
    navController: NavHostController = rememberNavController()
) = remember(navController) {
    AppRouter(navController)
}
