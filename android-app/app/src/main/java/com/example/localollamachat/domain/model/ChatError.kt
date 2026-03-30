package com.example.localollamachat.domain.model

enum class ChatError {
    LOCAL_MODEL_UNAVAILABLE,
    MODEL_NOT_RUNNING,
    TIMEOUT,
    RATE_LIMITED,
    INPUT_TOO_LONG,
    CONTEXT_TOO_LONG,
    MALFORMED_RESPONSE,
    INVALID_SERVER_URL,
    UNKNOWN
}

fun ChatError.toUserMessage(): String = when (this) {
    ChatError.LOCAL_MODEL_UNAVAILABLE -> "Локальная модель недоступна"
    ChatError.MODEL_NOT_RUNNING -> "Модель gemma3 не запущена"
    ChatError.TIMEOUT -> "Превышено время ожидания ответа"
    ChatError.RATE_LIMITED -> "Слишком много запросов, попробуйте позже"
    ChatError.INPUT_TOO_LONG -> "Слишком длинный запрос"
    ChatError.CONTEXT_TOO_LONG -> "Слишком длинный контекст"
    ChatError.MALFORMED_RESPONSE -> "Ошибка формата ответа модели"
    ChatError.INVALID_SERVER_URL -> "Некорректный адрес сервера"
    ChatError.UNKNOWN -> "Неизвестная ошибка"
}
