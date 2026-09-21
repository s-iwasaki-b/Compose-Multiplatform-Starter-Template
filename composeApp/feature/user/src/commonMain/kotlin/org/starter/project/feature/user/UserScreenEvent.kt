package org.starter.project.feature.user

import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface UserScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : UserScreenEvent
    data object OnClickBack : UserScreenEvent
    data object OnSnackBarShown : UserScreenEvent
}

internal object UserScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        appRouter: AppRouter,
        viewModel: UserScreenViewModel,
        onRefreshArticles: () -> Unit
    ) {
        when (event) {
            UserScreenEvent.OnClickErrorScreenAction -> {
                onRefreshArticles()
            }
            UserScreenEvent.OnClickBack -> {
                appRouter.popBackStack()
            }
            UserScreenEvent.OnSnackBarShown -> {
                viewModel.onSnackBarShown()
            }
            else -> {
                /* no-op */
            }
        }
    }
}
