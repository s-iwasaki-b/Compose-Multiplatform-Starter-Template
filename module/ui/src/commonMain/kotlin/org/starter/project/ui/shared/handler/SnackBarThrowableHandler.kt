package org.starter.project.ui.shared.handler

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.starter.project.ui.shared.state.ScreenState
import org.starter.project.ui.shared.state.SnackBarState

object SnackBarThrowableHandler {
    inline operator fun invoke(state: MutableStateFlow<ScreenState>): (Throwable) -> Unit = { t ->
        state.update {
            it.copy(
                snackBarState = SnackBarState(t.message.orEmpty())
            )
        }
    }
}
