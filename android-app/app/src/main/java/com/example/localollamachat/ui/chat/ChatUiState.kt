package com.example.localollamachat.ui.chat

import com.example.localollamachat.config.ChatConfig
import com.example.localollamachat.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val serverUrl: String = ChatConfig.defaultServerUrl,
    val serverUrlDraft: String = ChatConfig.defaultServerUrl,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)
