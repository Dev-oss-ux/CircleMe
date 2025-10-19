package com.barry.circleme.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Circle(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val memberIds: List<String> = emptyList()
)
