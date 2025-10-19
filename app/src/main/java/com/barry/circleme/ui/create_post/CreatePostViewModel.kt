package com.barry.circleme.ui.create_post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class CreatePostViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    private val _postCreated = MutableStateFlow(false)
    val postCreated = _postCreated.asStateFlow()

    fun onTextChange(newText: String) {
        _text.value = newText
    }

    fun createPost() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null && _text.value.isNotBlank()) {
                try {
                    // Fetch the user's latest profile data from Firestore
                    val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                    val user = userDoc.toObject(User::class.java)

                    val newPost = Post(
                        authorId = currentUser.uid,
                        authorName = user?.displayName ?: "Anonymous",
                        authorPhotoUrl = user?.photoUrl ?: "",
                        text = _text.value,
                        timestamp = Date()
                    )
                    firestore.collection("posts").add(newPost).await()
                    
                    _text.value = "" // Clear the text field
                    _postCreated.value = true // Signal that post was created

                } catch (e: Exception) {
                    Log.e("CreatePostViewModel", "Error creating post", e)
                    // Optionally, show an error message to the user
                }
            } 
        }
    }

    fun onPostCreatedHandled() {
        _postCreated.value = false
    }
}
