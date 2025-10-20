package com.barry.circleme.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.User
import com.barry.circleme.ui.create_post.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class FollowStatus {
    NOT_FOLLOWING,
    FOLLOWING,
    REQUESTED
}

class ProfileViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _profileUser = MutableStateFlow<User?>(null)
    val profileUser = _profileUser.asStateFlow()

    private val _displayName = MutableStateFlow(auth.currentUser?.displayName ?: "")
    val displayName = _displayName.asStateFlow()

    private val _photoUrl = MutableStateFlow(auth.currentUser?.photoUrl?.toString() ?: "")
    val photoUrl = _photoUrl.asStateFlow()

    private val _postCount = MutableStateFlow(0)
    val postCount = _postCount.asStateFlow()

    private val _followersCount = MutableStateFlow(0)
    val followersCount = _followersCount.asStateFlow()

    private val _followingCount = MutableStateFlow(0)
    val followingCount = _followingCount.asStateFlow()

    private val _followStatus = MutableStateFlow(FollowStatus.NOT_FOLLOWING)
    val followStatus = _followStatus.asStateFlow()

    private val _isCurrentUserProfile = MutableStateFlow(false)
    val isCurrentUserProfile = _isCurrentUserProfile.asStateFlow()

    private val _userPostsWithImages = MutableStateFlow<List<Post>>(emptyList())
    val userPostsWithImages = _userPostsWithImages.asStateFlow()

    private val _bookmarkedPosts = MutableStateFlow<List<Post>>(emptyList())
    val bookmarkedPosts = _bookmarkedPosts.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut = _signedOut.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadProfile(null)
    }

    fun loadProfile(userId: String?) {
        _isLoading.value = true
        val targetUserId = userId ?: auth.currentUser?.uid
        _isCurrentUserProfile.value = userId == null || userId == auth.currentUser?.uid

        if (targetUserId != null) {
            firestore.collection("users").document(targetUserId).addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                _profileUser.value = user
                _displayName.value = user?.displayName ?: ""
                _photoUrl.value = user?.photoUrl ?: ""
                _followersCount.value = user?.followers?.size ?: 0
                _followingCount.value = user?.following?.size ?: 0
                user?.bookmarkedPosts?.let { fetchBookmarkedPosts(it) }
                updateFollowStatus(targetUserId)
                _isLoading.value = false
            }

            auth.currentUser?.uid?.let { currentUid ->
                firestore.collection("users").document(currentUid).addSnapshotListener { snapshot, _ ->
                    _user.value = snapshot?.toObject(User::class.java)
                    updateFollowStatus(targetUserId)
                }
            }

            fetchUserPosts(targetUserId)
        }
    }

    private fun updateFollowStatus(targetUserId: String) {
        val currentUser = _user.value
        when {
            currentUser?.following?.contains(targetUserId) == true -> {
                _followStatus.value = FollowStatus.FOLLOWING
            }
            currentUser?.outgoingFollowRequests?.contains(targetUserId) == true -> {
                _followStatus.value = FollowStatus.REQUESTED
            }
            else -> {
                _followStatus.value = FollowStatus.NOT_FOLLOWING
            }
        }
    }

    private fun fetchUserPosts(userId: String) {
        viewModelScope.launch {
            try {
                val documents = firestore.collection("posts")
                    .whereEqualTo("authorId", userId)
                    .get()
                    .await()
                val posts: List<Post> = documents.toObjects(Post::class.java)
                _postCount.value = posts.size
                _userPostsWithImages.value = posts.filter { !it.imageUrl.isNullOrBlank() }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching user posts", e)
            }
        }
    }

    private fun fetchBookmarkedPosts(postIds: List<String>) {
        if (postIds.isEmpty()) {
            _bookmarkedPosts.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val documents = firestore.collection("posts")
                    .whereIn(FieldPath.documentId(), postIds)
                    .get()
                    .await()
                _bookmarkedPosts.value = documents.toObjects(Post::class.java)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching bookmarked posts", e)
            }
        }
    }

    fun handleFollowAction() {
        viewModelScope.launch {
            val currentUid = auth.currentUser?.uid ?: return@launch
            val targetUid = _profileUser.value?.uid ?: return@launch

            if (currentUid == targetUid) return@launch

            val currentUserRef = firestore.collection("users").document(currentUid)
            val targetUserRef = firestore.collection("users").document(targetUid)

            when (_followStatus.value) {
                FollowStatus.NOT_FOLLOWING -> {
                    // Send follow request
                    currentUserRef.update("outgoingFollowRequests", FieldValue.arrayUnion(targetUid))
                    targetUserRef.update("pendingFollowRequests", FieldValue.arrayUnion(currentUid))
                }
                FollowStatus.FOLLOWING -> {
                    // Unfollow
                    currentUserRef.update("following", FieldValue.arrayRemove(targetUid))
                    targetUserRef.update("followers", FieldValue.arrayRemove(currentUid))
                }
                FollowStatus.REQUESTED -> {
                    // Cancel follow request
                    currentUserRef.update("outgoingFollowRequests", FieldValue.arrayRemove(targetUid))
                    targetUserRef.update("pendingFollowRequests", FieldValue.arrayRemove(currentUid))
                }
            }
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
                saveProfile()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading profile image", e)
            }
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch

            try {
                val profileUpdates = userProfileChangeRequest {
                    displayName = _displayName.value
                    if (_photoUrl.value.isNotBlank()) {
                        photoUri = Uri.parse(_photoUrl.value)
                    }
                }
                user.updateProfile(profileUpdates).await()

                val userUpdates = mapOf(
                    "displayName" to _displayName.value,
                    "photoUrl" to _photoUrl.value
                )
                firestore.collection("users").document(user.uid)
                    .set(userUpdates, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving profile", e)
            }
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
