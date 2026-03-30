package com.example.localollamachat.config

import com.example.localollamachat.BuildConfig

object ChatConfig {
    const val OLLAMA_CHAT_PATH = "api/chat"
    val defaultServerUrl: String = BuildConfig.DEFAULT_SERVER_URL
    val defaultModel: String = BuildConfig.DEFAULT_MODEL
    val maxInputChars: Int = BuildConfig.MAX_INPUT_CHARS
    val maxContextMessages: Int = BuildConfig.MAX_CONTEXT_MESSAGES
    val maxContextChars: Int = BuildConfig.MAX_CONTEXT_CHARS
    val maxRequestsPerMinute: Int = BuildConfig.MAX_REQUESTS_PER_MINUTE
    val rateLimitWindowMs: Long = BuildConfig.RATE_LIMIT_WINDOW_MS
    val requestTimeoutSeconds: Int = BuildConfig.REQUEST_TIMEOUT_SECONDS
}
