package com.barry.circleme.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun onEmailChange(newValue: String) {
        _email.value = newValue.trim()
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    private suspend fun ensureUserDocumentExists(user: FirebaseUser) {
        val userDocRef = firestore.collection("users").document(user.uid)
        val document = userDocRef.get().await()
        if (!document.exists()) {
            val newUser = User(
                uid = user.uid,
                displayName = user.displayName ?: user.email?.substringBefore('@'),
                photoUrl = user.photoUrl?.toString() ?: ""
            )
            userDocRef.set(newUser).await()
        }
    }

    fun signInWithEmailPassword() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // 1. Try to sign in. This will fail if the user does not exist.
                val authResult = auth.signInWithEmailAndPassword(_email.value, _password.value).await()
                ensureUserDocumentExists(authResult.user!!) 
                _uiState.value = AuthUiState.Success(authResult.user!!)

            } catch (e: Exception) {
                // 2. If sign in fails, assume the user does not exist and try to create a new account.
                try {
                    val authResult = auth.createUserWithEmailAndPassword(_email.value, _password.value).await()
                    val user = authResult.user!!
                    // Set username as display name for new email/password accounts
                    val username = _email.value.substringBefore('@')
                    val profileUpdates = userProfileChangeRequest { displayName = username }
                    user.updateProfile(profileUpdates).await()
                    // Create the user document in Firestore for the new user
                    ensureUserDocumentExists(user)
                    _uiState.value = AuthUiState.Success(user)
                } catch (signUpException: FirebaseAuthUserCollisionException) {
                     _uiState.value = AuthUiState.Error("Incorrect password or another sign-up error.")
                } catch (signUpException: Exception) {
                    _uiState.value = AuthUiState.Error(signUpException.message ?: "Sign-up failed.")
                }
            }
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                ensureUserDocumentExists(authResult.user!!)
                _uiState.value = AuthUiState.Success(authResult.user!!)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
