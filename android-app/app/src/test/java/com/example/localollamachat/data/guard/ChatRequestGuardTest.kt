package com.example.localollamachat.data.guard

import com.example.localollamachat.domain.model.ChatError
import com.example.localollamachat.domain.model.ChatMessage
import com.example.localollamachat.domain.model.ChatRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRequestGuardTest {
    private val guard = ChatRequestGuard(
        maxInputChars = 10,
        maxContextMessages = 3,
        maxContextChars = 20,
        maxRequestsPerWindow = 2,
        rateLimitWindowMs = 60_000
    )

    @Test
    fun `returns input too long when user prompt exceeds limit`() {
        val history = emptyList<ChatMessage>()
        val newMessage = ChatMessage(role = ChatRole.USER, content = "12345678901")

        val result = guard.buildContext(history, newMessage)

        assertTrue(result is ChatRequestGuard.ContextResult.Error)
        val error = result as ChatRequestGuard.ContextResult.Error
        assertEquals(ChatError.INPUT_TOO_LONG, error.error)
    }

    @Test
    fun `rate limit blocks request when window quota is exceeded`() {
        val first = guard.checkRateLimit(nowMs = 1_000)
        val second = guard.checkRateLimit(nowMs = 2_000)
        val third = guard.checkRateLimit(nowMs = 3_000)

        assertEquals(null, first)
        assertEquals(null, second)
        assertEquals(ChatError.RATE_LIMITED, third)
    }

    @Test
    fun `context too long when total chars exceed limit`() {
        val history = listOf(
            ChatMessage(role = ChatRole.USER, content = "1234567"),
            ChatMessage(role = ChatRole.ASSISTANT, content = "1234567")
        )
        val newMessage = ChatMessage(role = ChatRole.USER, content = "1234567")

        val result = guard.buildContext(history, newMessage)

        assertTrue(result is ChatRequestGuard.ContextResult.Error)
        val error = result as ChatRequestGuard.ContextResult.Error
        assertEquals(ChatError.CONTEXT_TOO_LONG, error.error)
    }
}
