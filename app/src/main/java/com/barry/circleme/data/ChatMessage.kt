package com.barry.circleme.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class MessageType {
    TEXT, VOICE
}

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val type: MessageType = MessageType.TEXT,

    // For text messages
    val text: String? = null,

    // For voice messages
    val audioUrl: String? = null,
    val duration: Long? = null, // Duration in seconds

    @ServerTimestamp
    val timestamp: Date? = null
)
