package com.barry.circleme.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.Conversation
import com.barry.circleme.utils.TimeUtils
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
                ConversationItem(
                    conversation = conversation,
                    currentUserId = currentUserId ?: "",
                    onClick = { onConversationClick(it.recipientId, it.recipientName) }
                )
                Divider()
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    onClick: (ConversationClickData) -> Unit
) {
    val otherParticipantId = conversation.participantIds.firstOrNull { it != currentUserId } ?: ""
    val otherParticipantName = conversation.participantNames[otherParticipantId] ?: "Unknown User"
    val otherParticipantPhoto = conversation.participantPhotos?.get(otherParticipantId) ?: ""

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick(ConversationClickData(otherParticipantId, otherParticipantName)) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (otherParticipantPhoto.isNotBlank()) {
            AsyncImage(
                model = otherParticipantPhoto,
                contentDescription = "Profile picture of $otherParticipantName",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default profile picture",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = otherParticipantName,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        conversation.lastMessageTimestamp?.let {
            Text(
                text = TimeUtils.formatTimestamp(it),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

data class ConversationClickData(
    val recipientId: String,
    val recipientName: String
)
