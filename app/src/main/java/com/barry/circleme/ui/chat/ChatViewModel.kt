package com.barry.circleme.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.barry.circleme.data.ChatMessage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val recipientId: String = checkNotNull(savedStateHandle["recipientId"])
    
    // --- Safe Initialization --- 
    private var conversationRef: DocumentReference? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _newMessageText = MutableStateFlow("")
    val newMessageText = _newMessageText.asStateFlow()

    init {
        // Only initialize if the user is logged in. Otherwise, the screen will just be empty.
        auth.currentUser?.uid?.let { currentUserId ->
            val conversationId = getConversationId(currentUserId, recipientId)
            conversationRef = firestore.collection("conversations").document(conversationId)
            fetchMessages()
        }
    }

    private fun fetchMessages() {
        conversationRef?.collection("messages")
            ?.orderBy("timestamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                _messages.value = snapshots?.toObjects(ChatMessage::class.java) ?: emptyList()
            }
    }

    fun onNewMessageChange(newText: String) {
        _newMessageText.value = newText
    }

    fun sendMessage() {
        val currentUser = auth.currentUser
        if (currentUser != null && _newMessageText.value.isNotBlank()) {
            val messageText = _newMessageText.value
            val message = ChatMessage(
                senderId = currentUser.uid,
                receiverId = recipientId,
                text = messageText
            )

            conversationRef?.collection("messages")?.add(message)
                ?.addOnSuccessListener { 
                    _newMessageText.value = "" 
                }

            // Still update the conversation so it appears in the conversation list
            val conversationUpdate = mapOf(
                "lastMessage" to messageText,
                "lastMessageTimestamp" to FieldValue.serverTimestamp()
            )
            conversationRef?.update(conversationUpdate)
        }
    }

    private fun getConversationId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
