package com.barry.circleme.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.Comment
import com.barry.circleme.data.User
import com.barry.circleme.ui.create_post.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class HomeViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val posts: StateFlow<List<Post>> = combine(_allPosts, _searchQuery) { allPosts, query ->
        if (query.isBlank()) {
            allPosts
        } else {
            allPosts.filter { it.text.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _likerNames = MutableStateFlow<List<String>>(emptyList())
    val likerNames = _likerNames.asStateFlow()

    init {
        fetchAllPosts()
    }

    private fun fetchAllPosts() {
        _isLoading.value = true
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("HomeViewModel", "Listen failed.", e)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _allPosts.value = snapshots.toObjects(Post::class.java)
                }
                _isLoading.value = false
            }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onLikeClick(postId: String) {
        val currentUser = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(postId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val post = snapshot.toObject(Post::class.java)!!
            val likedBy = post.likedBy.toMutableList()

            if (likedBy.contains(currentUser.uid)) {
                likedBy.remove(currentUser.uid)
            } else {
                likedBy.add(currentUser.uid)
            }

            transaction.update(postRef, "likedBy", likedBy)
            null
        }.addOnFailureListener { e ->
            Log.e("HomeViewModel", "Like transaction failed", e)
        }
    }

    fun fetchLikers(postId: String) {
        viewModelScope.launch {
            try {
                val post = firestore.collection("posts").document(postId).get().await().toObject(Post::class.java)
                val userIds = post?.likedBy ?: return@launch
                if (userIds.isNotEmpty()) {
                    val users = firestore.collection("users").whereIn("uid", userIds).get().await()
                        .toObjects(User::class.java)
                    _likerNames.value = users.map { it.displayName ?: it.username }
                } else {
                    _likerNames.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching likers", e)
            }
        }
    }

    fun toggleBookmark(postId: String) {
        val currentUser = auth.currentUser ?: return
        val userRef = firestore.collection("users").document(currentUser.uid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject(User::class.java)
            val bookmarkedPosts = user?.bookmarkedPosts ?: emptyList()
            if (bookmarkedPosts.contains(postId)) {
                transaction.update(userRef, "bookmarkedPosts", FieldValue.arrayRemove(postId))
            } else {
                transaction.update(userRef, "bookmarkedPosts", FieldValue.arrayUnion(postId))
            }
            null
        }.addOnFailureListener { e ->
            Log.e("HomeViewModel", "Bookmark transaction failed", e)
        }
    }

    fun addComment(postId: String, postAuthorId: String, commentText: String) {
        if (commentText.isBlank()) return
        if (postId.isBlank()) {
            Log.e("HomeViewModel", "Cannot comment on post with blank ID.")
            return
        }
        val currentUser = auth.currentUser ?: return
        val comment = Comment(
            id = UUID.randomUUID().toString(),
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "",
            text = commentText,
            timestamp = Date()
        )

        firestore.collection("posts").document(postId)
            .update("comments", FieldValue.arrayUnion(comment))
            .addOnSuccessListener {
                // Create a notification for the post author
                val notification = com.barry.circleme.data.Notification(
                    userId = postAuthorId, // The ID of the user who receives the notification
                    actorId = currentUser.uid, // The ID of the user who performed the action
                    actorName = currentUser.displayName ?: "",
                    postId = postId,
                    type = com.barry.circleme.data.NotificationType.COMMENT
                )
                if (postAuthorId != currentUser.uid) { // Avoid notifying yourself
                    firestore.collection("notifications").add(notification)
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeViewModel", "Failed to add comment", e)
            }
    }

    fun likeComment(postId: String, commentId: String) {
        val currentUser = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(postId)

        firestore.runTransaction { transaction ->
            val post = transaction.get(postRef).toObject(Post::class.java)!!
            val comments = post.comments.map { comment ->
                if (comment.id == commentId) {
                    val likedBy = comment.likedBy.toMutableList()
                    if (likedBy.contains(currentUser.uid)) {
                        likedBy.remove(currentUser.uid)
                    } else {
                        likedBy.add(currentUser.uid)
                    }
                    comment.copy(likedBy = likedBy)
                } else {
                    comment
                }
            }
            transaction.update(postRef, "comments", comments)
            null
        }.addOnFailureListener { e ->
            Log.e("HomeViewModel", "Like comment transaction failed", e)
        }
    }

    fun replyToComment(postId: String, commentId: String, replyText: String) {
        if (replyText.isBlank()) return
        val currentUser = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(postId)

        val reply = Comment(
            id = UUID.randomUUID().toString(),
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "",
            text = replyText,
            timestamp = Date()
        )

        firestore.runTransaction { transaction ->
            val post = transaction.get(postRef).toObject(Post::class.java)!!
            val comments = post.comments.map { comment ->
                if (comment.id == commentId) {
                    val replies = comment.replies.toMutableList().apply { add(reply) }
                    comment.copy(replies = replies)
                } else {
                    comment
                }
            }
            transaction.update(postRef, "comments", comments)
            null
        }.addOnFailureListener { e ->
            Log.e("HomeViewModel", "Reply to comment transaction failed", e)
        }
    }
}
