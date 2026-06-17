package org.starter.project.feature.chat

import org.starter.project.ui.shared.event.ScreenEvent

sealed interface ChatScreenEvent : ScreenEvent {
    data class OnInputChanged(val text: String) : ChatScreenEvent
    data object OnSendMessage : ChatScreenEvent
    data object OnToggleSettings : ChatScreenEvent
    data class OnApiKeyChanged(val key: String) : ChatScreenEvent
    data object OnSaveApiKey : ChatScreenEvent
    data object OnDismissError : ChatScreenEvent
}
