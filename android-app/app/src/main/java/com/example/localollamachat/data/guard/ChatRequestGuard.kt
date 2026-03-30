package com.example.localollamachat.data.guard

import com.example.localollamachat.domain.model.ChatError
import com.example.localollamachat.domain.model.ChatMessage

class ChatRequestGuard(
    private val maxInputChars: Int,
    private val maxContextMessages: Int,
    private val maxContextChars: Int,
    private val maxRequestsPerWindow: Int,
    private val rateLimitWindowMs: Long
) {
    private val timestampsMs = ArrayDeque<Long>()

    fun checkRateLimit(nowMs: Long = System.currentTimeMillis()): ChatError? {
        while (timestampsMs.isNotEmpty() && nowMs - timestampsMs.first() > rateLimitWindowMs) {
            timestampsMs.removeFirst()
        }

        return if (timestampsMs.size >= maxRequestsPerWindow) {
            ChatError.RATE_LIMITED
        } else {
            timestampsMs.addLast(nowMs)
            null
        }
    }

    fun buildContext(history: List<ChatMessage>, newMessage: ChatMessage): ContextResult {
        if (newMessage.content.length > maxInputChars) {
            return ContextResult.Error(ChatError.INPUT_TOO_LONG)
        }

        val trimmedContext = (history + newMessage).takeLast(maxContextMessages)
        val totalChars = trimmedContext.sumOf { it.content.length }
        if (totalChars > maxContextChars) {
            return ContextResult.Error(ChatError.CONTEXT_TOO_LONG)
        }

        return ContextResult.Ok(trimmedContext)
    }

    sealed interface ContextResult {
        data class Ok(val messages: List<ChatMessage>) : ContextResult
        data class Error(val error: ChatError) : ContextResult
    }
}
