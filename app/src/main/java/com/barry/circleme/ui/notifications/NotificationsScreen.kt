package com.barry.circleme.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.barry.circleme.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    notificationsViewModel: NotificationsViewModel = viewModel(),
    onNotificationClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val notifications by notificationsViewModel.notifications.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                actions = {
                    TextButton(onClick = { notificationsViewModel.clearAllNotifications() }) {
                        Text("Clear All")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Group notifications by time
            val groupedNotifications = notifications.groupBy { TimeUtils.formatTimestamp(it.timestamp) }
            groupedNotifications.forEach { (timeAgo, notifications) ->
                item {
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onNotificationClick = { onNotificationClick(notification.postId) },
                        onUserClick = { onUserClick(notification.actorId) },
                        notificationsViewModel = notificationsViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    notificationsViewModel: NotificationsViewModel,
    onNotificationClick: () -> Unit,
    onUserClick: () -> Unit,
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = notification.actorPhotoUrl,
            contentDescription = "User profile picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            val text = when (notification.type) {
                NotificationType.LIKE -> "**${notification.actorName}** liked your post."
                NotificationType.COMMENT -> "**${notification.actorName}** commented: '${notification.commentId}'"
                NotificationType.FOLLOW_REQUEST -> "**${notification.actorName}** started following you."
                NotificationType.COMMENT_LIKE -> "**${notification.actorName}** liked your comment: '${notification.commentId}'"
                else -> "New notification"
            }
            Text(text = text)
            notification.timestamp?.let {
                Text(
                    text = TimeUtils.formatTimestamp(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        if (notification.type == NotificationType.FOLLOW_REQUEST) {
            Button(
                onClick = { /* TODO: Follow back */ },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Follow Back", color = Color.White)
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
