package com.barry.circleme.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class MessageType {
    TEXT,
    VOICE
}

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val type: MessageType = MessageType.TEXT,
    val text: String? = null,
    val audioUrl: String? = null,
    val duration: Long? = null, // in seconds
    @ServerTimestamp val timestamp: Date? = null,
    val reactions: List<Reaction> = emptyList()
)

data class Reaction(
    val emoji: String = "",
    val userId: String = ""
)