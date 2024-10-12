package org.starter.project.feature.home

import androidx.compose.ui.focus.FocusManager
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
    inline operator fun invoke(
        event: ScreenEvent,
        viewModel: HomeScreenViewModel,
        focusManager: FocusManager,
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
                focusManager.clearFocus()
                articlesPagingItems.refresh()
            }
            else -> {
                /* no-op */
            }
        }
    }
}
