package com.barry.circleme.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val uid: String = "",
    val username: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null
)
