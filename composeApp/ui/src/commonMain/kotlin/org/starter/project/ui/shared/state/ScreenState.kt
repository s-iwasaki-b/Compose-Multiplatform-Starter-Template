package org.starter.project.ui.shared.state

import androidx.compose.runtime.Immutable

@Immutable
data class ScreenState(
    val screenLoadingState: ScreenLoadingState,
    val snackBarState: SnackBarState?
)

@Immutable
sealed class ScreenLoadingState(open val showLoading: Boolean) {
    data class Initial(override val showLoading: Boolean = false) : ScreenLoadingState(showLoading)
    data class Loading(override val showLoading: Boolean = true) : ScreenLoadingState(showLoading)
    data class Success(override val showLoading: Boolean = false) : ScreenLoadingState(showLoading)
    data class Failure(
        override val showLoading: Boolean = false,
        val throwable: Throwable
    ) : ScreenLoadingState(showLoading)
}

@Immutable
data class SnackBarState(
    val message: String
)
