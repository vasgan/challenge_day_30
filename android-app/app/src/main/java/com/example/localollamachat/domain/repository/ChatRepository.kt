package com.example.localollamachat.domain.repository

import com.example.localollamachat.domain.model.ChatError
import com.example.localollamachat.domain.model.ChatMessage

interface ChatRepository {
    suspend fun sendMessage(
        serverUrl: String,
        contextMessages: List<ChatMessage>
    ): ChatResponse
}

sealed interface ChatResponse {
    data class Success(val assistantText: String) : ChatResponse
    data class Failure(val error: ChatError) : ChatResponse
}
