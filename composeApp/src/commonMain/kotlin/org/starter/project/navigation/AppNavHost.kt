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
import org.starter.project.ui.route.AppRoute

/**
 * https://developer.android.com/guide/navigation/design/type-safety
 */
@Composable
internal fun AppNavHost(
    appRouter: AppRouterImpl
) {
    NavHost(appRouter.navController, startDestination = AppRoute.Home()) {
        composable<AppRoute.Home>(
            deepLinks = listOf(
                navDeepLink<AppRoute.Home>(
                    basePath = "${DeepLinkConfig.SCHEME}://home"
                )
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Home>()
            HomeScreen(
                viewModel = koinViewModel(),
                appRouter = appRouter,
                deepLinkKeyword = route.keyword
            )
        }
        composable<AppRoute.User>(
            deepLinks = listOf(
                navDeepLink<AppRoute.User>(
                    basePath = "${DeepLinkConfig.SCHEME}://user"
                )
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.User>()
            val viewModel = koinViewModel<UserScreenViewModel> { parametersOf(route.navArgs) }
            UserScreen(viewModel = viewModel, appRouter = appRouter)
        }
    }
}
