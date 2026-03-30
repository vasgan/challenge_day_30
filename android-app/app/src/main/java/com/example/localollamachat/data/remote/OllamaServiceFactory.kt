package com.example.localollamachat.data.remote

import com.example.localollamachat.config.ChatConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class OllamaServiceFactory {
    private val cache = ConcurrentHashMap<String, OllamaApi>()

    fun getApi(baseUrl: String): OllamaApi {
        val normalized = normalizeBaseUrl(baseUrl)
        return cache.getOrPut(normalized) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(ChatConfig.requestTimeoutSeconds.toLong(), TimeUnit.SECONDS)
                .readTimeout(ChatConfig.requestTimeoutSeconds.toLong(), TimeUnit.SECONDS)
                .writeTimeout(ChatConfig.requestTimeoutSeconds.toLong(), TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            Retrofit.Builder()
                .baseUrl(normalized)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OllamaApi::class.java)
        }
    }

    private fun normalizeBaseUrl(raw: String): String {
        val value = raw.trim()
        require(value.isNotEmpty()) { "Base URL is empty" }

        val withScheme = if (value.startsWith("http://") || value.startsWith("https://")) {
            value
        } else {
            "http://$value"
        }

        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }
}
