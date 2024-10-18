package org.starter.project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.starter.project.feature.home.HomeScreen
import org.starter.project.ui.route.Route

/**
 * https://developer.android.com/guide/navigation/design/type-safety
 */
@Composable
fun AppNavHost(
    appRouter: AppRouter
) {
    NavHost(appRouter.navController, startDestination = Route.Home) {
        composable<Route.Home> {
            HomeScreen(appRouter = appRouter)
        }
    }
}
