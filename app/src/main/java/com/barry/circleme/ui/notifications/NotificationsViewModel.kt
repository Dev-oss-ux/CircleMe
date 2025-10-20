package com.barry.circleme.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.Notification
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications = _hasUnreadNotifications.asStateFlow()

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, _ ->
                    val notificationList = snapshots?.toObjects(Notification::class.java) ?: emptyList()
                    _notifications.value = notificationList
                    _hasUnreadNotifications.value = notificationList.any { !it.read }

                    // Mark notifications as read
                    snapshots?.documents?.forEach { document ->
                        if (document.getBoolean("read") == false) {
                            document.reference.update("read", true)
                        }
                    }
                }
        }
    }

    fun acceptFollowRequest(notification: Notification) {
        val currentUser = auth.currentUser ?: return
        val actorId = notification.actorId

        val currentUserRef = firestore.collection("users").document(currentUser.uid)
        val actorRef = firestore.collection("users").document(actorId)

        firestore.runBatch { 
            // Add to followers/following
            currentUserRef.update("followers", FieldValue.arrayUnion(actorId))
            actorRef.update("following", FieldValue.arrayUnion(currentUser.uid))

            // Clean up requests
            currentUserRef.update("pendingFollowRequests", FieldValue.arrayRemove(actorId))
            actorRef.update("outgoingFollowRequests", FieldValue.arrayRemove(currentUser.uid))

            // Delete notification
            firestore.collection("notifications").document(notification.id).delete()
        }
    }

    fun declineFollowRequest(notification: Notification) {
        val currentUser = auth.currentUser ?: return
        val actorId = notification.actorId

        val currentUserRef = firestore.collection("users").document(currentUser.uid)
        val actorRef = firestore.collection("users").document(actorId)

        firestore.runBatch {
            // Clean up requests
            currentUserRef.update("pendingFollowRequests", FieldValue.arrayRemove(actorId))
            actorRef.update("outgoingFollowRequests", FieldValue.arrayRemove(currentUser.uid))

            // Delete notification
            firestore.collection("notifications").document(notification.id).delete()
        }
    }
}
