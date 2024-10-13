package org.starter.project.ui.shared.handler

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.starter.project.ui.shared.state.ScreenLoadingState
import org.starter.project.ui.shared.state.ScreenState

object ErrorScreenThrowableHandler {
    inline operator fun invoke(state: MutableStateFlow<ScreenState>): (Throwable) -> Unit = { t ->
        state.update {
            it.copy(
                screenLoadingState = ScreenLoadingState.Failure(
                    showLoading = false,
                    throwable = t
                )
            )
        }
    }
}
