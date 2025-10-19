package com.barry.circleme.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.barry.circleme.data.User
import com.barry.circleme.utils.TimeUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    modifier: Modifier = Modifier,
    conversationsViewModel: ConversationsViewModel = viewModel(),
    onConversationClick: (recipientId: String) -> Unit
) {
    val conversations by conversationsViewModel.conversations.collectAsState()
    val users by conversationsViewModel.users.collectAsState()
    val currentUserId = Firebase.auth.currentUser?.uid
    var showUserMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                actions = {
                    Box {
                        IconButton(onClick = { showUserMenu = !showUserMenu }) {
                            Icon(Icons.Default.Add, contentDescription = "New Conversation")
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            users.forEach { user ->
                                DropdownMenuItem(
                                    text = { Text(user.displayName ?: "") },
                                    onClick = { 
                                        conversationsViewModel.startConversation(user) { 
                                            onConversationClick(user.uid)
                                        }
                                        showUserMenu = false
                                     }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(conversations) { conversation ->
                val unreadCount = conversation.unreadCount[currentUserId] ?: 0
                ConversationItem(
                    conversation = conversation,
                    currentUserId = currentUserId ?: "",
                    unreadCount = unreadCount,
                    onClick = { onConversationClick(it) }
                )
                Divider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationItem(
    conversation: Conversation,
    currentUserId: String,
    unreadCount: Int,
    onClick: (String) -> Unit
) {
    val otherParticipantId = conversation.participantIds.firstOrNull { it != currentUserId } ?: ""
    val otherParticipantName = conversation.participantNames[otherParticipantId] ?: "Unknown User"
    val otherParticipantPhoto = conversation.participantPhotos?.get(otherParticipantId) ?: ""
    val isUnread = unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick(otherParticipantId) }
            .background(if (isUnread) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
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
            if (isUnread) {
                 Badge {
                     Text(unreadCount.toString())
                 }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = otherParticipantName,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                fontSize = 17.sp
            )
            Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnread) MaterialTheme.colorScheme.onSurface else Color.Gray,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
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
