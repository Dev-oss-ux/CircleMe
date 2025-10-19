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

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    init {
        fetchConversations()
        fetchAllUsers()
    }

    private fun fetchAllUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val allUsers = documents.toObjects(User::class.java)
                _users.value = allUsers.filter { it.uid != currentUserId }
            }
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
            "participantPhotos" to mapOf(
                currentUserId to (currentUser.photoUrl?.toString() ?: ""),
                otherUserId to (user.photoUrl ?: "")
            ),
            "lastMessage" to "",
            "lastMessageTimestamp" to FieldValue.serverTimestamp()
        )

        conversationRef.set(conversationData, SetOptions.merge())
            .addOnSuccessListener { 
                onComplete(conversationId) 
            }
    }
    
    private fun getConversationId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
