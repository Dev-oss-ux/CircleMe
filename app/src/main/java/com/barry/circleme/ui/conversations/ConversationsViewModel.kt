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
import java.util.Date

class ConversationsViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount = _totalUnreadCount.asStateFlow()

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
                val convs = snapshots?.toObjects(Conversation::class.java) ?: emptyList()
                _conversations.value = convs
                _totalUnreadCount.value = convs.sumOf { (it.unreadCount[currentUser.uid] ?: 0L).toInt() }
            }
    }

    fun startConversation(user: User, initialMessage: String? = null, onComplete: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid
        val otherUserId = user.uid

        val conversationId = getConversationId(currentUserId, otherUserId)
        val conversationRef = firestore.collection("conversations").document(conversationId)

        // Optimistic local update: create a Conversation object and prepend it to the list so it appears immediately
        val optimisticConversation = Conversation(
            id = conversationId,
            participantIds = listOf(currentUserId, otherUserId).sorted(),
            participantNames = mapOf(
                currentUserId to (currentUser.displayName ?: ""),
                otherUserId to (user.displayName ?: "")
            ),
            participantPhotos = mapOf(
                currentUserId to (currentUser.photoUrl?.toString() ?: ""),
                otherUserId to (user.photoUrl ?: "")
            ),
            lastMessage = (initialMessage ?: ""),
            lastMessageTimestamp = Date(),
            unreadCount = mapOf(otherUserId to 1, currentUserId to 0)
        )

        // remove any existing conversation with same id and add the optimistic one at top
        _conversations.value = listOf(optimisticConversation) + _conversations.value.filter { it.id != conversationId }

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
            "lastMessage" to (initialMessage ?: ""),
            "lastMessageTimestamp" to FieldValue.serverTimestamp()
        )

        conversationRef.set(conversationData, SetOptions.merge())
            .addOnSuccessListener {
                if (!initialMessage.isNullOrBlank()) {
                    val messageData = mapOf(
                        "senderId" to currentUserId,
                        "text" to initialMessage,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    // add message to subcollection
                    conversationRef.collection("messages").add(messageData)
                        .addOnSuccessListener {
                            // update last message and unread count for the other participant
                            val updates = mapOf<String, Any>(
                                "lastMessage" to initialMessage,
                                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                                "unreadCount.$otherUserId" to FieldValue.increment(1)
                            )
                            conversationRef.set(updates, SetOptions.merge())
                                .addOnSuccessListener {
                                    onComplete(conversationId)
                                }
                                .addOnFailureListener {
                                    // still call onComplete so caller can continue
                                    onComplete(conversationId)
                                }
                        }
                        .addOnFailureListener {
                            onComplete(conversationId)
                        }
                } else {
                    onComplete(conversationId)
                }
            }
            .addOnFailureListener {
                onComplete(conversationId)
            }
    }
    
    private fun getConversationId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
