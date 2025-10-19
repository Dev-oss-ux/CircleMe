package com.barry.circleme.ui.circle

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.Circle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CircleViewModel : ViewModel() {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _circleName = MutableStateFlow("")
    val circleName = _circleName.asStateFlow()

    private val _circleCreated = MutableStateFlow(false)
    val circleCreated = _circleCreated.asStateFlow()

    private val _userCircle = MutableStateFlow<Circle?>(null)
    val userCircle = _userCircle.asStateFlow()

    init {
        fetchUserCircle()
    }

    private fun fetchUserCircle() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("circles")
                .whereArrayContains("memberIds", currentUser.uid)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val circle = documents.documents[0].toObject(Circle::class.java)?.copy(id = documents.documents[0].id)
                        _userCircle.value = circle
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("CircleViewModel", "Error getting user circle: ", exception)
                }
        }
    }

    fun onCircleNameChange(newName: String) {
        _circleName.value = newName
    }

    fun createCircle() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null && _circleName.value.isNotBlank()) {
                val newCircle = Circle(
                    ownerId = currentUser.uid,
                    name = _circleName.value,
                    memberIds = listOf(currentUser.uid)
                )
                firestore.collection("circles").add(newCircle)
                    .addOnSuccessListener { 
                        _circleCreated.value = true 
                    }
                    .addOnFailureListener { 
                        // TODO: Handle error
                    }
            }
        }
    }

    fun generateAndShareInviteLink(context: Context) {
        val circleId = _userCircle.value?.id ?: return
        val inviteLink = "https://www.circleme.app/invite?circleId=$circleId"

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Join my circle on CircleMe! $inviteLink")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }
    
    fun joinCircle(circleId: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("circles").document(circleId)
                .update("memberIds", FieldValue.arrayUnion(currentUser.uid))
                .addOnSuccessListener {
                    Log.d("CircleViewModel", "User ${currentUser.uid} successfully joined circle $circleId")
                    // TODO: Navigate to home screen
                }
                .addOnFailureListener { e ->
                    Log.w("CircleViewModel", "Error joining circle", e)
                }
        }
    }

    fun onCircleCreatedHandled() {
        _circleCreated.value = false
    }
}
