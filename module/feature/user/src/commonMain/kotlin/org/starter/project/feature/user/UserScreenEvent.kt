package org.starter.project.feature.user

import androidx.paging.compose.LazyPagingItems
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface UserScreenEvent : ScreenEvent {
    data object OnTapErrorScreenAction : UserScreenEvent
    data object OnTapBack : UserScreenEvent
}

internal object UserScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        articlesPagingItems: LazyPagingItems<Article>,
        onNavigateBack: () -> Unit,
    ) {
        when (event) {
            UserScreenEvent.OnTapErrorScreenAction -> {
                articlesPagingItems.refresh()
            }
            UserScreenEvent.OnTapBack -> {
                onNavigateBack()
            }
            else -> {
                /* no-op */
            }
        }
    }
}
