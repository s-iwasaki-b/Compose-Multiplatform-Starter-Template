package org.starter.project.feature.home

import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface HomeScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : HomeScreenEvent
    data class OnChangeSearchKeyword(val keyword: String) : HomeScreenEvent
    data object OnClickClearSearchKeyword : HomeScreenEvent
    data object OnClickActionSearchKeyword : HomeScreenEvent
    data class OnClickUser(val username: String) : HomeScreenEvent
    data object OnPullToRefresh : HomeScreenEvent
    data object OnPullToRefreshComplete : HomeScreenEvent
    data object OnSnackBarShown : HomeScreenEvent
}

internal object HomeScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        appRouter: AppRouter,
        viewModel: HomeScreenViewModel,
        onRefreshArticles: () -> Unit
    ) {
        when (event) {
            HomeScreenEvent.OnClickErrorScreenAction -> {
                onRefreshArticles()
            }

            is HomeScreenEvent.OnChangeSearchKeyword -> {
                viewModel.updateSearchKeyword(event.keyword)
            }

            HomeScreenEvent.OnClickClearSearchKeyword -> {
                viewModel.updateSearchKeyword("")
                onRefreshArticles()
            }

            HomeScreenEvent.OnClickActionSearchKeyword -> {
                onRefreshArticles()
            }

            is HomeScreenEvent.OnClickUser -> {
                appRouter.navigate(AppRoute.User(event.username))
            }

            HomeScreenEvent.OnPullToRefresh -> {
                viewModel.updatePullToRefreshing(true)
                onRefreshArticles()
            }

            HomeScreenEvent.OnPullToRefreshComplete -> {
                viewModel.updatePullToRefreshing(false)
            }

            HomeScreenEvent.OnSnackBarShown -> {
                viewModel.onSnackBarShown()
            }

            else -> {
                /* no-op */
            }
        }
    }
}
