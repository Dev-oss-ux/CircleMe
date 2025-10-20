package com.barry.circleme.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.Notification
import com.barry.circleme.data.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    notificationsViewModel: NotificationsViewModel = viewModel(),
    onNotificationClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    val notifications by notificationsViewModel.notifications.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Notifications") }) }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(
                    notification = notification,
                    notificationsViewModel = notificationsViewModel,
                    onNotificationClick = { onNotificationClick(notification.postId) },
                    onUserClick = { onUserClick(notification.actorId) }
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification, 
    notificationsViewModel: NotificationsViewModel, 
    onNotificationClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val unreadColor = when (notification.type) {
        NotificationType.LIKE -> Color(0xFFFDE7E7)
        NotificationType.COMMENT -> Color(0xFFE7F3FF)
        NotificationType.FOLLOW_REQUEST -> Color(0xFFE7F9E7)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    }
    val backgroundColor = if (notification.read) Color.Transparent else unreadColor
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        tonalElevation = if (notification.read) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if(notification.type == NotificationType.FOLLOW_REQUEST) {
                        onUserClick()
                    } else {
                        onNotificationClick()
                    }
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AsyncImage(
                    model = notification.actorPhotoUrl,
                    contentDescription = "User profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.padding(start = 16.dp))
                Column {
                    val text = when (notification.type) {
                        NotificationType.LIKE -> "${notification.actorName} liked your post."
                        NotificationType.COMMENT -> "${notification.actorName} commented on your post."
                        NotificationType.FOLLOW_REQUEST -> "${notification.actorName} wants to follow you."
                        else -> "New notification"
                    }
                    Text(text = text, fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal)
                }
            }
            if (notification.type == NotificationType.FOLLOW_REQUEST) {
                 TextButton(onClick = { notificationsViewModel.declineFollowRequest(notification) }) {
                    Text("Decline")
                }
            } else if (notification.postPreviewUrl != null) {
                AsyncImage(
                    model = notification.postPreviewUrl,
                    contentDescription = "Post preview",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
