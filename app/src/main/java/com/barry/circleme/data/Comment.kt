package com.barry.circleme.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Comment(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val text: String = "",
    val likedBy: List<String> = emptyList(),
    val replies: List<Comment> = emptyList(),
    val timestamp: Date? = null
)
