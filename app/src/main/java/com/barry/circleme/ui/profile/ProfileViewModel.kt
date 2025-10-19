package com.barry.circleme.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.User
import com.barry.circleme.ui.create_post.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    private val _displayName = MutableStateFlow(auth.currentUser?.displayName ?: "")
    val displayName = _displayName.asStateFlow()

    private val _photoUrl = MutableStateFlow(auth.currentUser?.photoUrl?.toString() ?: "")
    val photoUrl = _photoUrl.asStateFlow()

    private val _postCount = MutableStateFlow(0)
    val postCount = _postCount.asStateFlow()

    private val _likeCount = MutableStateFlow(0)
    val likeCount = _likeCount.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut = _signedOut.asStateFlow()

    init {
        // Fetch the latest user data from Firestore to ensure UI is up-to-date
        auth.currentUser?.uid?.let {
            firestore.collection("users").document(it).addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                _displayName.value = user?.displayName ?: ""
                _photoUrl.value = user?.photoUrl ?: ""
            }
        }

        fetchUserStats()
    }

    private fun fetchUserStats() {
        val user = auth.currentUser ?: return
        firestore.collection("posts")
            .whereEqualTo("authorId", user.uid)
            .get()
            .addOnSuccessListener { documents ->
                val posts: List<Post> = documents.toObjects(Post::class.java)
                _postCount.value = posts.size
                _likeCount.value = posts.sumOf { it.likedBy.size }
            }
    }

    fun onDisplayNameChange(newName: String) {
        _displayName.value = newName
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val storageRef = storage.reference.child("profile_pictures/${user.uid}")

            try {
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                _photoUrl.value = downloadUrl.toString()
                saveProfile() // Immediately save the new photo url
            } catch (e: Exception) {
                // Handle upload error
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch

            // 1. Update Firebase Auth profile
            val profileUpdates = userProfileChangeRequest {
                displayName = _displayName.value
                if (_photoUrl.value.isNotBlank()) {
                    photoUri = Uri.parse(_photoUrl.value)
                }
            }
            user.updateProfile(profileUpdates).await()

            // 2. Update user document in Firestore using the robust set/merge option
            val userUpdates = mapOf(
                "displayName" to _displayName.value,
                "photoUrl" to _photoUrl.value
            )
            firestore.collection("users").document(user.uid)
                .set(userUpdates, SetOptions.merge())
                .await()
        }
    }

    fun signOut() {
        auth.signOut()
        _signedOut.value = true
    }

    fun onSignedOutHandled() {
        _signedOut.value = false
    }
}
