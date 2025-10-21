package com.barry.circleme.ui.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.barry.circleme.data.Comment
import com.barry.circleme.ui.create_post.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.UUID

class CommentsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val postRef = firestore.collection("posts").document(postId)

    private val _post = MutableStateFlow<Post?>(null)
    val post = _post.asStateFlow()

    private val _newCommentText = MutableStateFlow("")
    val newCommentText = _newCommentText.asStateFlow()

    init {
        fetchPost()
    }

    private fun fetchPost() {
        postRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }
            _post.value = snapshot?.toObject(Post::class.java)
        }
    }

    fun onNewCommentChange(text: String) {
        _newCommentText.value = text
    }

    fun addComment() {
        val commentText = _newCommentText.value
        if (commentText.isBlank()) return

        val currentUser = auth.currentUser ?: return
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "",
            authorPhotoUrl = currentUser.photoUrl?.toString(),
            text = commentText,
            timestamp = Date()
        )

        postRef.update("comments", FieldValue.arrayUnion(newComment))
            .addOnSuccessListener {
                _newCommentText.value = ""
                // Optionally create notification
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun likeComment(commentId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentPost = _post.value ?: return

        val updatedComments = currentPost.comments.map { comment ->
            if (comment.id == commentId) {
                val likedBy = comment.likedBy.toMutableList()
                if (likedBy.contains(currentUserId)) {
                    likedBy.remove(currentUserId)
                } else {
                    likedBy.add(currentUserId)
                    // Optionally create notification
                }
                comment.copy(likedBy = likedBy)
            } else {
                comment
            }
        }
        postRef.update("comments", updatedComments)
    }
}
