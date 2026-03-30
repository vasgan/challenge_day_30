package com.example.localollamachat.domain.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: ChatRole,
    val content: String,
    val createdAtMs: Long = System.currentTimeMillis()
)
