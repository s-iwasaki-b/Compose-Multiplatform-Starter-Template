package org.starter.project.feature.home

import androidx.compose.runtime.Immutable
import org.starter.project.ui.shared.state.ScreenState

@Immutable
internal data class HomeScreenState(
    val screenState: ScreenState,
    val searchKeyword: String = ""
)
