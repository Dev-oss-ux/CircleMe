package com.barry.circleme.ui.create_post

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val text: String = "",
    val imageUrl: String? = null, // Added image url
    @ServerTimestamp
    val timestamp: Date? = null,
    val likedBy: List<String> = emptyList()
)
