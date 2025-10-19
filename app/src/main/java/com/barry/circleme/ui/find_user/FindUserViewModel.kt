package com.barry.circleme.ui.find_user

import androidx.lifecycle.ViewModel
import com.barry.circleme.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FindUserViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    init {
        fetchAllUsers()
    }

    private fun fetchAllUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val allUsers = documents.toObjects(User::class.java)
                // Filter out the current user on the client side
                _users.value = allUsers.filter { it.uid != currentUserId }
            }
            .addOnFailureListener {
                // Handle error
            }
    }
}
