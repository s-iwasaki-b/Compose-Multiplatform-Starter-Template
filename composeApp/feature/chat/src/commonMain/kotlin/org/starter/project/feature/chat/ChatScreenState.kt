package org.starter.project.feature.chat

import androidx.compose.runtime.Immutable

@Immutable
data class ChatScreenState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isProcessing: Boolean = false,
    val apiKey: String = "",
    val isApiKeyConfigured: Boolean = false,
    val showSettings: Boolean = false,
    val error: String? = null
)
