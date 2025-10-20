package com.barry.circleme.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class MessageType {
    TEXT, VOICE
}

data class Reaction(
    val emoji: String = "",
    val userId: String = ""
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val type: MessageType = MessageType.TEXT,

    // For text messages
    val text: String? = null,

    // For voice messages
    val audioUrl: String? = null,
    val duration: Long? = null, // Duration in seconds

    val reactions: List<Reaction> = emptyList(),

    @ServerTimestamp
    val timestamp: Date? = null
)
