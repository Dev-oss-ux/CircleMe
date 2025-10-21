package com.barry.circleme.ui.new_conversation

import androidx.lifecycle.ViewModel
import com.barry.circleme.data.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewConversationViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    init {
        Firebase.firestore.collection("users").get()
            .addOnSuccessListener { result ->
                _users.value = result.toObjects<User>()
            }
    }
}
