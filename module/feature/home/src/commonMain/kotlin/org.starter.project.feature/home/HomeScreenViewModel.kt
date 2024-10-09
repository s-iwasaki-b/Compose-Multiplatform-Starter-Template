package org.starter.project.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.starter.project.ui.shared.state.ScreenLoadingState
import org.starter.project.ui.shared.state.ScreenState

class HomeScreenViewModel : ViewModel() {
    private val _screenState = MutableStateFlow(
        ScreenState(ScreenLoadingState.Initial(true), null)
    )
    private val _state = MutableStateFlow(
        HomeScreenState(screenState = _screenState.value)
    )
    internal val state = combine(
        _screenState,
        _state
    ) { screenState, state ->
        state.copy(screenState = screenState)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        _state.value
    )

    fun refresh() {
        // TODO: implement refresh logic
    }

    fun updateSearchKeyword(keyword: String) {
        _state.update {
            it.copy(
                searchKeyword = keyword
            )
        }
    }
}
