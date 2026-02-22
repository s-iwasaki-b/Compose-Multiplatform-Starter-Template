package org.starter.project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.starter.project.feature.home.HomeScreen
import org.starter.project.feature.user.UserScreen
import org.starter.project.feature.user.UserScreenViewModel
import org.starter.project.ui.route.Route

/**
 * https://developer.android.com/guide/navigation/design/type-safety
 */
@Composable
fun AppNavHost(
    appRouter: AppRouter
) {
    NavHost(appRouter.navController, startDestination = Route.Home()) {
        composable<Route.Home>(
            deepLinks = listOf(
                navDeepLink<Route.Home>(
                    basePath = "${DeepLinkConfig.SCHEME}://home"
                )
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Home>()
            HomeScreen(
                appRouter = appRouter,
                deepLinkKeyword = route.keyword
            )
        }
        composable<Route.User>(
            deepLinks = listOf(
                navDeepLink<Route.User>(
                    basePath = "${DeepLinkConfig.SCHEME}://user"
                )
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<Route.User>()
            val viewModel = koinViewModel<UserScreenViewModel> {
                parametersOf(route.username)
            }
            UserScreen(viewModel = viewModel, appRouter = appRouter)
        }
    }
}
