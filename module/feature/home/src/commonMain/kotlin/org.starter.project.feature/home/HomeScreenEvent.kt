package org.starter.project.feature.home

import org.starter.project.ui.shared.event.ScreenEvent
import org.starter.project.ui.shared.event.ScreenEventHandler

internal sealed interface HomeScreenEvent : ScreenEvent {

}

internal class HomeScreenEventHandler(
    viewModel: HomeScreenViewModel
) : ScreenEventHandler {
    override operator fun invoke(event: ScreenEvent) {
        when (event) {
            else -> {
                /* no-op */
            }
        }
    }
}
