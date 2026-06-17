package org.starter.project.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavUri
import androidx.navigation.navOptions
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.starter.project.domain.ai.action.AppAction
import org.starter.project.domain.ai.action.AppActionDispatcher
import org.starter.project.feature.chat.ChatDrawer
import org.starter.project.feature.chat.ChatScreenViewModel
import org.starter.project.feature.chat.rememberChatDrawerState
import org.starter.project.navigation.AppNavHost
import org.starter.project.navigation.DeepLinkHandler
import org.starter.project.navigation.rememberAppRouter
import org.starter.project.ui.design.system.theme.SystemTheme
import org.starter.project.ui.route.AppRoute

@Composable
fun Main() {
    val appRouter = rememberAppRouter()
    val chatDrawerState = rememberChatDrawerState()

    KoinContext {
        val dispatcher = koinInject<AppActionDispatcher>()
        val chatViewModel = koinViewModel<ChatScreenViewModel>()

        LaunchedEffect(dispatcher) {
            dispatcher.actions.collect { action ->
                when (action) {
                    is AppAction.SearchArticles -> {
                        appRouter.navigate(AppRoute.Home(keyword = action.keyword))
                    }
                    is AppAction.NavigateToUser -> {
                        appRouter.navigate(AppRoute.User(action.username))
                    }
                }
            }
        }

        SystemTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SystemTheme.colors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                ChatDrawer(
                    drawerState = chatDrawerState,
                    viewModel = chatViewModel
                ) {
                    AppNavHost(appRouter)
                }

                DisposableEffect(Unit) {
                    DeepLinkHandler.listener = { uri ->
                        appRouter.navController.navigate(
                            NavUri(uri),
                            navOptions { launchSingleTop = true }
                        )
                    }
                    onDispose {
                        DeepLinkHandler.listener = null
                    }
                }
            }
        }
    }
}
