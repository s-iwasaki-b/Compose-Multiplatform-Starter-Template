package org.starter.project.feature.user

import androidx.compose.runtime.Immutable
import org.starter.project.base.data.model.zenn.User
import org.starter.project.ui.shared.state.ScreenState

@Immutable
internal data class UserScreenState(
    val screenState: ScreenState,
    val username: String? = null,
    val user: User? = null,
)
