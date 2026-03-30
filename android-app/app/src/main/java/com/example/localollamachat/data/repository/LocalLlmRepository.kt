package com.example.localollamachat.data.repository

import com.example.localollamachat.data.remote.OllamaServiceFactory
import com.example.localollamachat.data.remote.model.OllamaChatRequest
import com.example.localollamachat.data.remote.model.OllamaMessageDto
import com.example.localollamachat.domain.model.ChatError
import com.example.localollamachat.domain.model.ChatMessage
import java.io.IOException
import java.net.SocketTimeoutException

class LocalLlmRepository(
    private val serviceFactory: OllamaServiceFactory
) {
    suspend fun sendChat(
        serverUrl: String,
        model: String,
        messages: List<ChatMessage>
    ): LocalLlmResult {
        val request = OllamaChatRequest(
            model = model,
            messages = messages.map { message ->
                OllamaMessageDto(role = message.role.toApiValue(), content = message.content)
            },
            stream = false
        )

        return try {
            val response = serviceFactory.getApi(serverUrl).chat(request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                LocalLlmResult.Failure(mapHttpError(response.code(), errorBody))
            } else {
                val content = response.body()?.message?.content
                if (content.isNullOrBlank()) {
                    LocalLlmResult.Failure(ChatError.MALFORMED_RESPONSE)
                } else {
                    LocalLlmResult.Success(content)
                }
            }
        } catch (_: IllegalArgumentException) {
            LocalLlmResult.Failure(ChatError.INVALID_SERVER_URL)
        } catch (_: SocketTimeoutException) {
            LocalLlmResult.Failure(ChatError.TIMEOUT)
        } catch (_: IOException) {
            LocalLlmResult.Failure(ChatError.LOCAL_MODEL_UNAVAILABLE)
        } catch (_: Exception) {
            LocalLlmResult.Failure(ChatError.UNKNOWN)
        }
    }

    private fun mapHttpError(code: Int, errorBody: String): ChatError {
        val normalized = errorBody.lowercase()
        return when {
            code == 404 && normalized.contains("model") -> ChatError.MODEL_NOT_RUNNING
            code == 429 -> ChatError.RATE_LIMITED
            code == 504 -> ChatError.TIMEOUT
            code == 503 -> ChatError.LOCAL_MODEL_UNAVAILABLE
            code == 413 && normalized.contains("context") -> ChatError.CONTEXT_TOO_LONG
            code == 413 -> ChatError.INPUT_TOO_LONG
            normalized.contains("model") && normalized.contains("not found") -> ChatError.MODEL_NOT_RUNNING
            normalized.contains("timeout") -> ChatError.TIMEOUT
            else -> ChatError.UNKNOWN
        }
    }

    sealed interface LocalLlmResult {
        data class Success(val assistantText: String) : LocalLlmResult
        data class Failure(val error: ChatError) : LocalLlmResult
    }
}
