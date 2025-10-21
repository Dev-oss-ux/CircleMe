package com.barry.circleme.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barry.circleme.data.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Hashtag(val name: String)

class DiscoverViewModel : ViewModel() {

    private val firestore = Firebase.firestore

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _trendingHashtags = MutableStateFlow<List<Hashtag>>(emptyList())
    val trendingHashtags = _trendingHashtags.asStateFlow()

    private val _suggestedAccounts = MutableStateFlow<List<User>>(emptyList())
    val suggestedAccounts = _suggestedAccounts.asStateFlow()

    init {
        // Mock data for now
        _trendingHashtags.value = listOf(Hashtag("#SunriseLovers"), Hashtag("#UrbanJungle"), Hashtag("#Foodie"))
        fetchSuggestedAccounts()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchUsers(query)
        } else {
            _users.value = emptyList()
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            val querySnapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .await()
            _users.value = querySnapshot.toObjects(User::class.java)
        }
    }

    private fun fetchSuggestedAccounts() {
        viewModelScope.launch {
            val querySnapshot = firestore.collection("users")
                .limit(5)
                .get()
                .await()
            _suggestedAccounts.value = querySnapshot.toObjects(User::class.java)
        }
    }
}
