package com.barry.circleme.ui.home

import androidx.lifecycle.ViewModel
import com.barry.circleme.data.Comment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommentViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    fun fetchComments(postId: String) {
        firestore.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                _comments.value = snapshots?.toObjects(Comment::class.java) ?: emptyList()
            }
    }

    fun addComment(postId: String, commentText: String) {
        val currentUser = auth.currentUser ?: return
        if (commentText.isBlank()) return

        val comment = Comment(
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "Anonymous",
            authorPhotoUrl = currentUser.photoUrl?.toString(),
            text = commentText
        )

        firestore.collection("posts").document(postId)
            .collection("comments")
            .add(comment)
    }
}
