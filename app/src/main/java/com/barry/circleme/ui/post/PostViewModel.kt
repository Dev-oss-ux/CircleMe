package com.barry.circleme.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.ui.create_post.Post
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val _post = MutableStateFlow<Post?>(null)
    val post = _post.asStateFlow()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            val postDocument = firestore.collection("posts").document(postId).get().await()
            _post.value = postDocument.toObject(Post::class.java)
        }
    }
}
