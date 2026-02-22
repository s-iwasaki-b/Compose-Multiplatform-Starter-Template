package org.starter.project.feature.user

import androidx.paging.compose.LazyPagingItems
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.ui.route.AppRouter
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface UserScreenEvent : ScreenEvent {
    data object OnClickErrorScreenAction : UserScreenEvent
    data object OnClickBack : UserScreenEvent
}

internal object UserScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        appRouter: AppRouter,
        articlesPagingItems: LazyPagingItems<Article>
    ) {
        when (event) {
            UserScreenEvent.OnClickErrorScreenAction -> {
                articlesPagingItems.refresh()
            }
            UserScreenEvent.OnClickBack -> {
                appRouter.popBackStack()
            }
            else -> {
                /* no-op */
            }
        }
    }
}
