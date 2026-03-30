package com.example.localollamachat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localollamachat.config.ChatConfig
import com.example.localollamachat.data.guard.ChatRequestGuard
import com.example.localollamachat.data.remote.OllamaServiceFactory
import com.example.localollamachat.data.repository.ChatRepositoryImpl
import com.example.localollamachat.data.repository.LocalLlmRepository
import com.example.localollamachat.domain.model.ChatMessage
import com.example.localollamachat.domain.model.ChatRole
import com.example.localollamachat.domain.model.toUserMessage
import com.example.localollamachat.domain.repository.ChatRepository
import com.example.localollamachat.domain.repository.ChatResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepositoryImpl(
        modelName = ChatConfig.defaultModel,
        localLlmRepository = LocalLlmRepository(OllamaServiceFactory())
    ),
    private val guard: ChatRequestGuard = ChatRequestGuard(
        maxInputChars = ChatConfig.maxInputChars,
        maxContextMessages = ChatConfig.maxContextMessages,
        maxContextChars = ChatConfig.maxContextChars,
        maxRequestsPerWindow = ChatConfig.maxRequestsPerMinute,
        rateLimitWindowMs = ChatConfig.rateLimitWindowMs
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    role = ChatRole.SYSTEM,
                    content = "Локальный чат использует Ollama + gemma3."
                )
            )
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputChanged(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

    fun onServerUrlDraftChanged(value: String) {
        _uiState.update { it.copy(serverUrlDraft = value) }
    }

    fun applyServerUrl() {
        _uiState.update {
            it.copy(
                serverUrl = it.serverUrlDraft.trim(),
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun sendMessage() {
        val snapshot = _uiState.value
        if (snapshot.isSending) return

        val userText = snapshot.inputText.trim()
        if (userText.isEmpty()) return

        val userMessage = ChatMessage(role = ChatRole.USER, content = userText)

        val rateLimitError = guard.checkRateLimit()
        if (rateLimitError != null) {
            setError(rateLimitError.toUserMessage())
            return
        }

        val contextResult = guard.buildContext(snapshot.messages, userMessage)
        val contextMessages = when (contextResult) {
            is ChatRequestGuard.ContextResult.Ok -> contextResult.messages
            is ChatRequestGuard.ContextResult.Error -> {
                setError(contextResult.error.toUserMessage())
                return
            }
        }

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isSending = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            when (
                val result = chatRepository.sendMessage(
                    serverUrl = _uiState.value.serverUrl,
                    contextMessages = contextMessages
                )
            ) {
                is ChatResponse.Success -> {
                    val assistant = ChatMessage(role = ChatRole.ASSISTANT, content = result.assistantText)
                    _uiState.update {
                        it.copy(
                            messages = it.messages + assistant,
                            isSending = false
                        )
                    }
                }
                is ChatResponse.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = result.error.toUserMessage()
                        )
                    }
                }
            }
        }
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
