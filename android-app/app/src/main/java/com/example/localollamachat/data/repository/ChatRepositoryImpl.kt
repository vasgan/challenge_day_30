package com.example.localollamachat.data.repository

import com.example.localollamachat.domain.model.ChatMessage
import com.example.localollamachat.domain.repository.ChatRepository
import com.example.localollamachat.domain.repository.ChatResponse

class ChatRepositoryImpl(
    private val modelName: String,
    private val localLlmRepository: LocalLlmRepository
) : ChatRepository {
    override suspend fun sendMessage(serverUrl: String, contextMessages: List<ChatMessage>): ChatResponse {
        return when (val result = localLlmRepository.sendChat(serverUrl, modelName, contextMessages)) {
            is LocalLlmRepository.LocalLlmResult.Success -> {
                ChatResponse.Success(result.assistantText)
            }
            is LocalLlmRepository.LocalLlmResult.Failure -> {
                ChatResponse.Failure(result.error)
            }
        }
    }
}
