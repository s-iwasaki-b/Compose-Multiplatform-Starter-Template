package org.starter.project.feature.chat

import androidx.compose.runtime.Immutable

@Immutable
data class ChatMessage(
    val id: String,
    val content: String,
    val role: ChatRole,
    val timestamp: Long
)

enum class ChatRole {
    USER,
    ASSISTANT
}
