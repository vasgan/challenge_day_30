package com.example.localollamachat.data.remote.model

data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessageDto>,
    val stream: Boolean = false
)

data class OllamaMessageDto(
    val role: String,
    val content: String
)

data class OllamaChatResponse(
    val model: String? = null,
    val created_at: String? = null,
    val message: OllamaMessageDto? = null,
    val done: Boolean? = null,
    val error: String? = null
)
