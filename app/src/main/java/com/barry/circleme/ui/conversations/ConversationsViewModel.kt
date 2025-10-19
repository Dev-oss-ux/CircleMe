package com.barry.circleme.ui.conversations

import androidx.lifecycle.ViewModel
import com.barry.circleme.data.Conversation
import com.barry.circleme.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConversationsViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    init {
        fetchConversations()
    }

    private fun fetchConversations() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("conversations")
            .whereArrayContains("participantIds", currentUser.uid)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                _conversations.value = snapshots?.toObjects(Conversation::class.java) ?: emptyList()
            }
    }

    fun startConversation(user: User, onComplete: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid
        val otherUserId = user.uid

        val conversationId = getConversationId(currentUserId, otherUserId)
        val conversationRef = firestore.collection("conversations").document(conversationId)

        val conversationData = mapOf(
            "id" to conversationId,
            "participantIds" to listOf(currentUserId, otherUserId).sorted(),
            "participantNames" to mapOf(
                currentUserId to (currentUser.displayName ?: ""),
                otherUserId to (user.displayName ?: "")
            ),
            "lastMessage" to "", // Add a default empty message to ensure the document is created correctly
            "lastMessageTimestamp" to FieldValue.serverTimestamp()
        )

        // Use set with merge to create the conversation if it doesn't exist,
        // or just update the timestamp if it does.
        conversationRef.set(conversationData, SetOptions.merge())
            .addOnSuccessListener { 
                onComplete(conversationId) 
            }
    }
    
    private fun getConversationId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
