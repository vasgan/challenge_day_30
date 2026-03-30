package com.example.localollamachat.data.remote

import com.example.localollamachat.config.ChatConfig
import com.example.localollamachat.data.remote.model.OllamaChatRequest
import com.example.localollamachat.data.remote.model.OllamaChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApi {
    @POST(ChatConfig.OLLAMA_CHAT_PATH)
    suspend fun chat(@Body request: OllamaChatRequest): Response<OllamaChatResponse>
}
