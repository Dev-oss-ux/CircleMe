package com.barry.circleme.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio = _bio.asStateFlow()

    private val _profileSaveState = MutableStateFlow(false)
    val profileSaveState = _profileSaveState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        auth.currentUser?.uid?.let {
            firestore.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    _user.value = user
                    _displayName.value = user?.displayName ?: ""
                    _bio.value = user?.bio ?: ""
                }
        }
    }

    fun onDisplayNameChange(newName: String) {
        _displayName.value = newName
    }

    fun onBioChange(newBio: String) {
        _bio.value = newBio
    }

    fun saveProfile() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch

            val userUpdates = mapOf(
                "displayName" to _displayName.value,
                "bio" to _bio.value
            )

            firestore.collection("users").document(user.uid)
                .set(userUpdates, SetOptions.merge())
                .await()

            _profileSaveState.value = true
        }
    }
    
    fun onProfileSaveStateHandled() {
        _profileSaveState.value = false
    }
}
