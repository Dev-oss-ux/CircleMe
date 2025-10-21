package com.barry.circleme.ui.chat

import androidx.lifecycle.ViewModel
import com.barry.circleme.data.AiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AiChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<AiMessage>>(emptyList())
    val messages: StateFlow<List<AiMessage>> = _messages

    init {
        // Load initial message from the AI
        _messages.value = listOf(
            AiMessage(text = "Hello! I am your AI assistant. How can I help you today?", isMe = false)
        )
    }

    fun sendMessage(text: String) {
        val userMessage = AiMessage(text = text, isMe = true)
        _messages.value = _messages.value + userMessage

        // TODO: Replace with actual AI response logic
        val aiResponse = AiMessage(text = "Thanks for your message! I'll process it.", isMe = false)
        _messages.value = _messages.value + aiResponse
    }
}