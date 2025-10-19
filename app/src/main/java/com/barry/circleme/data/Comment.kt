package com.barry.circleme.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
