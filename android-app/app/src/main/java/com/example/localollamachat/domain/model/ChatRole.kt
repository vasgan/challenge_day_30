package com.example.localollamachat.domain.model

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM;

    fun toApiValue(): String = name.lowercase()
}
