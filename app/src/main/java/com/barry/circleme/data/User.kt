package com.barry.circleme.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val uid: String = "",
    val username: String = "",
    val displayName: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val bookmarkedPosts: List<String> = emptyList(),
    val pendingFollowRequests: List<String> = emptyList(),
    val outgoingFollowRequests: List<String> = emptyList()
)
