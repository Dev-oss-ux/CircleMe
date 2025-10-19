package com.barry.circleme.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.Notification
import com.google.firebase.auth.ktx.auth
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

    val hasUnreadNotifications = MutableStateFlow(false)

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            firestore.collection("notifications")
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener
                    val notificationList = snapshots?.toObjects(Notification::class.java) ?: emptyList()
                    _notifications.value = notificationList
                    hasUnreadNotifications.value = notificationList.any { !it.read }
                }
        }
    }

    fun markAllAsRead() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            firestore.collection("notifications")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.update("read", true)
                    }
                }
        }
    }
}
