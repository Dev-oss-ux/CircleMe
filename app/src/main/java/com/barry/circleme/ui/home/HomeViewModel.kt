package com.barry.circleme.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.Comment
import com.barry.circleme.data.Notification
import com.barry.circleme.data.NotificationType
import com.barry.circleme.data.User
import com.barry.circleme.ui.create_post.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _allPosts = MutableStateFlow<List<Post>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val posts = _searchQuery.combine(_allPosts) { query, posts ->
        if (query.isBlank()) {
            posts
        } else {
            posts.filter {
                it.text.contains(query, ignoreCase = true) ||
                it.authorName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _likerNames = MutableStateFlow<List<String>>(emptyList())
    val likerNames = _likerNames.asStateFlow()

    init {
        fetchAllPosts()
    }

    private fun fetchAllPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("HomeViewModel", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _allPosts.value = snapshots.toObjects(Post::class.java)
                }
            }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun deletePost(postId: String) {
        if (postId.isBlank()) {
            Log.e("HomeViewModel", "Cannot delete post with blank ID.")
            return
        }
        firestore.collection("posts").document(postId).delete()
    }

    fun editPost(postId: String, newText: String) {
        if (postId.isBlank()) {
            Log.e("HomeViewModel", "Cannot edit post with blank ID.")
            return
        }
        firestore.collection("posts").document(postId).update("text", newText)
    }

    fun getLikerNames(likedBy: List<String>) {
        if (likedBy.isEmpty()) {
            _likerNames.value = emptyList()
            return
        }
        firestore.collection("users")
            .whereIn("uid", likedBy)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.toObjects(User::class.java)
                _likerNames.value = users.map { it.displayName ?: "Unknown" }
            }
    }

    fun clearLikerNames() {
        _likerNames.value = emptyList()
    }

    fun toggleLike(postId: String, postAuthorId: String) {
        if (postId.isBlank()) {
            Log.e("HomeViewModel", "Cannot like post with blank ID.")
            return
        }
        val currentUser = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(postId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val post = snapshot.toObject(Post::class.java)
            val likedBy = post?.likedBy ?: emptyList()

            if (likedBy.contains(currentUser.uid)) {
                transaction.update(postRef, "likedBy", FieldValue.arrayRemove(currentUser.uid))
            } else {
                transaction.update(postRef, "likedBy", FieldValue.arrayUnion(currentUser.uid))
                if (currentUser.uid != postAuthorId) { // Don't notify for your own likes
                    val notification = Notification(
                        userId = postAuthorId,
                        actorId = currentUser.uid,
                        actorName = currentUser.displayName ?: "",
                        postId = postId,
                        type = NotificationType.LIKE
                    )
                    firestore.collection("notifications").add(notification)
                }
            }
            null // Transaction must return a value
        }.addOnFailureListener { e ->
            Log.e("HomeViewModel", "Like transaction failed", e)
        }
    }

    fun addComment(postId: String, postAuthorId: String, commentText: String) {
        if (postId.isBlank()) {
            Log.e("HomeViewModel", "Cannot comment on post with blank ID.")
            return
        }
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

        if (currentUser.uid != postAuthorId) {
            val notification = Notification(
                userId = postAuthorId,
                actorId = currentUser.uid,
                actorName = currentUser.displayName ?: "",
                postId = postId,
                type = NotificationType.COMMENT
            )
            firestore.collection("notifications").add(notification)
        }
    }
}
