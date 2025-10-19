package com.barry.circleme.ui.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.ChatMessage
import com.barry.circleme.data.MessageType
import com.barry.circleme.data.Reaction
import com.barry.circleme.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class VoiceRecordingState {
    IDLE,
    RECORDING,
    PREVIEW
}

class ChatViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage

    private val recipientId: String = checkNotNull(savedStateHandle["recipientId"])
    private var conversationRef: DocumentReference? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _newMessageText = MutableStateFlow("")
    val newMessageText = _newMessageText.asStateFlow()

    private val _voiceRecordingState = MutableStateFlow(VoiceRecordingState.IDLE)
    val voiceRecordingState = _voiceRecordingState.asStateFlow()

    private val _recipient = MutableStateFlow<User?>(null)
    val recipient = _recipient.asStateFlow()

    init {
        auth.currentUser?.uid?.let { currentUserId ->
            val conversationId = getConversationId(currentUserId, recipientId)
            conversationRef = firestore.collection("conversations").document(conversationId)
            fetchMessages()
            fetchRecipientDetails()
            // Mark messages as read when entering the chat
            conversationRef?.update("unreadCount.${auth.currentUser?.uid}", 0)
        }
    }

    private fun fetchRecipientDetails() {
        firestore.collection("users").document(recipientId).get()
            .addOnSuccessListener { document ->
                _recipient.value = document.toObject(User::class.java)
            }
    }

    private fun fetchMessages() {
        conversationRef?.collection("messages")
            ?.orderBy("timestamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { snapshots, e ->
                if (e != null) { return@addSnapshotListener }
                _messages.value = snapshots?.toObjects(ChatMessage::class.java) ?: emptyList()
            }
    }

    fun onNewMessageChange(newText: String) {
        _newMessageText.value = newText
    }
    
    fun onStartRecording() { _voiceRecordingState.value = VoiceRecordingState.RECORDING }
    fun onPreviewRecording() { _voiceRecordingState.value = VoiceRecordingState.PREVIEW }
    fun onCancelRecording() { _voiceRecordingState.value = VoiceRecordingState.IDLE }

    fun addReaction(messageId: String, emoji: String) {
        val currentUser = auth.currentUser ?: return
        val reaction = Reaction(emoji, currentUser.uid)

        conversationRef?.collection("messages")?.document(messageId)?.update("reactions", FieldValue.arrayUnion(reaction))
    }

    fun sendTextMessage() {
        val currentUser = auth.currentUser
        if (currentUser != null && _newMessageText.value.isNotBlank()) {
            val message = ChatMessage(
                senderId = currentUser.uid,
                receiverId = recipientId,
                type = MessageType.TEXT,
                text = _newMessageText.value
            )
            sendMessage(message, "lastMessage" to _newMessageText.value)
            _newMessageText.value = ""
        }
    }

    fun sendVoiceMessage(audioUri: Uri, duration: Long) {
        viewModelScope.launch {
            _voiceRecordingState.value = VoiceRecordingState.IDLE
            val currentUser = auth.currentUser ?: return@launch
            val storageRef = storage.reference.child("voice_messages/${System.currentTimeMillis()}")
            val uploadTask = storageRef.putFile(audioUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            val message = ChatMessage(
                senderId = currentUser.uid,
                receiverId = recipientId,
                type = MessageType.VOICE,
                audioUrl = downloadUrl,
                duration = duration
            )
            sendMessage(message, "lastMessage" to "🎤 Voice Message")
        }
    }

    private fun sendMessage(message: ChatMessage, lastMessageUpdate: Pair<String, Any>) {
        conversationRef?.collection("messages")?.add(message)

        val conversationUpdate = mapOf(
            lastMessageUpdate,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "unreadCount.${recipientId}" to FieldValue.increment(1) // Increment unread count for the other user
        )
        conversationRef?.update(conversationUpdate)
    }

    private fun getConversationId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }
}
