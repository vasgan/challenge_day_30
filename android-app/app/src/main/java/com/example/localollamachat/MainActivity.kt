package com.example.localollamachat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.localollamachat.ui.chat.ChatRoute
import com.example.localollamachat.ui.theme.LocalOllamaChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalOllamaChatTheme {
                ChatRoute()
            }
        }
    }
}
