package com.example.localollamachat.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localollamachat.domain.model.ChatMessage
import com.example.localollamachat.domain.model.ChatRole

@Composable
fun ChatRoute(viewModel: ChatViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        state = state,
        onInputChanged = viewModel::onInputChanged,
        onSendClick = viewModel::sendMessage,
        onServerUrlChanged = viewModel::onServerUrlDraftChanged,
        onApplyServerUrl = viewModel::applyServerUrl,
        onDismissError = viewModel::clearError
    )
}

@Composable
fun ChatScreen(
    state: ChatUiState,
    onInputChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onServerUrlChanged: (String) -> Unit,
    onApplyServerUrl: () -> Unit,
    onDismissError: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Local Ollama Chat (gemma3)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.serverUrlDraft,
                    onValueChange = onServerUrlChanged,
                    label = { Text("Server URL") },
                    singleLine = true
                )
                Button(onClick = onApplyServerUrl) {
                    Text("Apply")
                }
            }

            if (state.errorMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFE5E5))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.errorMessage,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF7A0000)
                    )
                    TextButton(onClick = onDismissError) {
                        Text("OK")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.inputText,
                    onValueChange = onInputChanged,
                    label = { Text("Введите сообщение") },
                    maxLines = 4
                )

                Button(
                    onClick = onSendClick,
                    enabled = !state.isSending
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val isSystem = message.role == ChatRole.SYSTEM

    val alignment: Alignment.Horizontal = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = when {
        isUser -> Color(0xFFD8EFFF)
        isSystem -> Color(0xFFEFEFEF)
        else -> Color(0xFFE5FFE7)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor)
                .padding(10.dp)
        ) {
            Text(text = message.content)
        }
    }
}
