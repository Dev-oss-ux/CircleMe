package com.barry.circleme.ui.conversations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    modifier: Modifier = Modifier,
    conversationsViewModel: ConversationsViewModel = viewModel(),
    onConversationClick: (recipientId: String, recipientName: String) -> Unit,
    onNewConversationClick: () -> Unit
) {
    val conversations by conversationsViewModel.conversations.collectAsState()
    val currentUserId = Firebase.auth.currentUser?.uid

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Messages") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewConversationClick) {
                Icon(Icons.Default.Add, contentDescription = "New Conversation")
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(conversations) { conversation ->
                // Find the other participant's ID and Name
                val otherParticipantId = conversation.participantIds.firstOrNull { it != currentUserId } ?: ""
                val otherParticipantName = conversation.participantNames[otherParticipantId] ?: "Unknown User"

                ListItem(
                    headlineContent = { Text(otherParticipantName) },
                    supportingContent = { Text(conversation.lastMessage) },
                    modifier = Modifier.clickable { onConversationClick(otherParticipantId, otherParticipantName) }
                )
            }
        }
    }
}
