package com.barry.circleme.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class NotificationType {
    LIKE, COMMENT, FOLLOW_REQUEST
}

data class Notification(
    @DocumentId val id: String = "",
    val userId: String = "", // The ID of the user who receives the notification
    val actorId: String = "", // The ID of the user who performed the action
    val actorName: String = "",
    val actorPhotoUrl: String? = null,
    val postId: String = "",
    val postPreviewUrl: String? = null,
    val type: NotificationType? = null,
    val read: Boolean = false,
    @ServerTimestamp val timestamp: Date? = null
)
