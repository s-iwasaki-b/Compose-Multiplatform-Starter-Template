package org.starter.project.feature.home

import app.cash.paging.compose.LazyPagingItems
import org.starter.project.base.data.model.zenn.Article
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface HomeScreenEvent : ScreenEvent {
    data object OnTapErrorScreenAction : HomeScreenEvent
    data class OnChangeSearchKeyword(val keyword: String) : HomeScreenEvent
    data object OnTapClearSearchKeyword : HomeScreenEvent
    data object OnTapActionSearchKeyword : HomeScreenEvent
}

internal object HomeScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        viewModel: HomeScreenViewModel,
        articlesPagingItems: LazyPagingItems<Article>
    ) {
        when (event) {
            HomeScreenEvent.OnTapErrorScreenAction -> {
                articlesPagingItems.refresh()
            }
            is HomeScreenEvent.OnChangeSearchKeyword -> {
                viewModel.updateSearchKeyword(event.keyword)
            }
            HomeScreenEvent.OnTapClearSearchKeyword -> {
                viewModel.updateSearchKeyword("")
                articlesPagingItems.refresh()
            }
            HomeScreenEvent.OnTapActionSearchKeyword -> {
                articlesPagingItems.refresh()
            }
            else -> {
                /* no-op */
            }
        }
    }
}
