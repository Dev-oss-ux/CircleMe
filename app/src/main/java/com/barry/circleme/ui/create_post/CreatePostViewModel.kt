package com.barry.circleme.ui.create_post

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class CreatePostViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    private val _postCreated = MutableStateFlow(false)
    val postCreated = _postCreated.asStateFlow()

    fun onTextChange(newText: String) {
        _text.value = newText
    }

    fun onImageSelected(uri: Uri) {
        _imageUri.value = uri
    }

    fun createPost() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null && (_text.value.isNotBlank() || _imageUri.value != null)) {
                try {
                    val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                    val user = userDoc.toObject(User::class.java)

                    var imageUrl: String? = null
                    _imageUri.value?.let {
                        val storageRef = storage.reference.child("post_images/${System.currentTimeMillis()}")
                        storageRef.putFile(it).await()
                        imageUrl = storageRef.downloadUrl.await().toString()
                    }

                    val newPost = Post(
                        authorId = currentUser.uid,
                        authorName = user?.displayName ?: "Anonymous",
                        authorPhotoUrl = user?.photoUrl ?: "",
                        text = _text.value,
                        imageUrl = imageUrl,
                        timestamp = Date()
                    )
                    firestore.collection("posts").add(newPost).await()
                    
                    _text.value = ""
                    _imageUri.value = null
                    _postCreated.value = true

                } catch (e: Exception) {
                    Log.e("CreatePostViewModel", "Error creating post", e)
                }
            } 
        }
    }

    fun onPostCreatedHandled() {
        _postCreated.value = false
    }
}
