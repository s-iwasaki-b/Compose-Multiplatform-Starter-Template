package org.starter.project.domain.ai.action

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppActionDispatcher {
    private val _actions = MutableSharedFlow<AppAction>(extraBufferCapacity = 16)
    val actions: SharedFlow<AppAction> = _actions.asSharedFlow()

    fun dispatch(action: AppAction) {
        _actions.tryEmit(action)
    }
}
