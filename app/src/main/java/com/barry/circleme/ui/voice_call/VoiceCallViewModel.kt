package com.barry.circleme.ui.voice_call

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.barry.circleme.data.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceCallViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val recipientId: String = savedStateHandle.get<String>("recipientId")!!
    private val _recipient = MutableStateFlow<User?>(null)
    val recipient = _recipient.asStateFlow()

    init {
        Firebase.firestore.collection("users").document(recipientId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    _recipient.value = document.toObject<User>()
                }
            }
    }
}
