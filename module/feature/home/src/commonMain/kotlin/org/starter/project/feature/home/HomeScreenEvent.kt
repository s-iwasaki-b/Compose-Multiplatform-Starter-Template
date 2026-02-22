package org.starter.project.feature.home

import androidx.paging.compose.LazyPagingItems
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.ui.route.AppRoute
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface HomeScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : HomeScreenEvent
    data class OnChangeSearchKeyword(val keyword: String) : HomeScreenEvent
    data object OnClickClearSearchKeyword : HomeScreenEvent
    data object OnClickActionSearchKeyword : HomeScreenEvent
    data class OnClickUser(val username: String) : HomeScreenEvent
}

internal object HomeScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        appRouter: AppRouter,
        viewModel: HomeScreenViewModel,
        articlesPagingItems: LazyPagingItems<Article>
    ) {
        when (event) {
            HomeScreenEvent.OnClickErrorScreenAction -> {
                articlesPagingItems.refresh()
            }

            is HomeScreenEvent.OnChangeSearchKeyword -> {
                viewModel.updateSearchKeyword(event.keyword)
            }

            HomeScreenEvent.OnClickClearSearchKeyword -> {
                viewModel.updateSearchKeyword("")
                articlesPagingItems.refresh()
            }

            HomeScreenEvent.OnClickActionSearchKeyword -> {
                articlesPagingItems.refresh()
            }

            is HomeScreenEvent.OnClickUser -> {
                appRouter.navigate(AppRoute.User(event.username))
            }

            else -> {
                /* no-op */
            }
        }
    }
}
