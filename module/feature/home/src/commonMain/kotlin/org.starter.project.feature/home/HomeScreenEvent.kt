package org.starter.project.feature.home

import org.starter.project.ui.shared.event.ScreenEvent

internal sealed interface HomeScreenEvent : ScreenEvent {
    data object OnTapErrorScreenAction : HomeScreenEvent
}

internal object HomeScreenEventHandler {
    operator fun invoke(
        event: ScreenEvent,
        viewModel: HomeScreenViewModel
    ) {
        when (event) {
            HomeScreenEvent.OnTapErrorScreenAction -> viewModel.refresh()
            else -> {
                /* no-op */
            }
        }
    }
}
