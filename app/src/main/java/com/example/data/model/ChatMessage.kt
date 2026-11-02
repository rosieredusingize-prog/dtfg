package com.example.data.model

enum class MessageSender {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: Long = 0,
    val assistantId: AssistantId,
    val sender: MessageSender,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val engineNote: String? = null
)
