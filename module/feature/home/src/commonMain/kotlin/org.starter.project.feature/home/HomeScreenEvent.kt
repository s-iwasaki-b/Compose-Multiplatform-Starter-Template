package org.starter.project.feature.home

import androidx.compose.ui.focus.FocusManager
import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface HomeScreenEvent : ScreenEvent {
    data object OnTapErrorScreenAction : HomeScreenEvent
    data class OnChangeSearchKeyword(val keyword: String) : HomeScreenEvent
    data object OnTapClearSearchKeyword : HomeScreenEvent
    data object OnTapCancelSearchKeyword : HomeScreenEvent
}

internal object HomeScreenEventHandler {
    inline operator fun invoke(
        event: ScreenEvent,
        viewModel: HomeScreenViewModel,
        focusManager: FocusManager
    ) {
        when (event) {
            HomeScreenEvent.OnTapErrorScreenAction -> {
                viewModel.refresh()
            }
            is HomeScreenEvent.OnChangeSearchKeyword -> {
                viewModel.updateSearchKeyword(event.keyword)
            }
            HomeScreenEvent.OnTapClearSearchKeyword -> {
                viewModel.updateSearchKeyword("")
            }
            HomeScreenEvent.OnTapCancelSearchKeyword -> {
                viewModel.updateSearchKeyword("")
                focusManager.clearFocus()
            }
            else -> {
                /* no-op */
            }
        }
    }
}
