package com.barry.circleme.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Story(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePictureUrl: String? = null,
    val imageUrl: String? = null,
    val text: String? = null,
    @ServerTimestamp val timestamp: Date? = null
)
