package com.barry.circleme.ui.edit_post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditPostViewModel : ViewModel() {

    private val firestore = Firebase.firestore

    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    private val _postUpdated = MutableStateFlow(false)
    val postUpdated = _postUpdated.asStateFlow()

    fun onTextChange(newText: String) {
        _text.value = newText
    }

    // This will be called when the screen is first opened
    fun setInitialText(initialText: String) {
        _text.value = initialText
    }

    fun updatePost(postId: String) {
        viewModelScope.launch {
            if (_text.value.isNotBlank()) {
                firestore.collection("posts").document(postId)
                    .update("text", _text.value)
                    .await()
                _postUpdated.value = true
            }
        }
    }

    fun onPostUpdatedHandled() {
        _postUpdated.value = false
    }
}
