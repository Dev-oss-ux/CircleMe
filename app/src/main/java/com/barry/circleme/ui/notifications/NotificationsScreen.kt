package com.barry.circleme.ui.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.Notification
import com.barry.circleme.data.NotificationType
import com.barry.circleme.utils.TimeUtils
import java.util.Calendar
import java.util.Date

fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

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
                title = { Text("Notifications", modifier = Modifier.fillMaxWidth()) },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val today = Date()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time

            val todayNotifications = notifications.filter { it.timestamp != null && isSameDay(it.timestamp, today) }
            val yesterdayNotifications = notifications.filter { it.timestamp != null && isSameDay(it.timestamp, yesterday) }
            val olderNotifications = notifications.filter { it.timestamp != null && !isSameDay(it.timestamp, today) && !isSameDay(it.timestamp, yesterday) }

            if (todayNotifications.isNotEmpty()) {
                item { Text("Today", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                items(todayNotifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onNotificationClick = { onNotificationClick(notification.postId) },
                        onUserClick = { onUserClick(notification.actorId) },
                        notificationsViewModel = notificationsViewModel
                    )
                }
            }

            if (yesterdayNotifications.isNotEmpty()) {
                item { Text("Yesterday", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                items(yesterdayNotifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onNotificationClick = { onNotificationClick(notification.postId) },
                        onUserClick = { onUserClick(notification.actorId) },
                        notificationsViewModel = notificationsViewModel
                    )
                }
            }
            
             if (olderNotifications.isNotEmpty()) {
                item { Text("Older", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                items(olderNotifications) { notification ->
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable {
             if(notification.type == NotificationType.FOLLOW_REQUEST) {
                    onUserClick()
                } else {
                    onNotificationClick()
                }
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NotificationActorImage(notification)

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                NotificationText(notification)
                notification.timestamp?.let {
                    Text(
                        text = TimeUtils.formatTimestamp(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            NotificationAction(notification, onUserClick)
        }
    }
}

@Composable
fun NotificationActorImage(notification: Notification) {
    Box(contentAlignment = Alignment.BottomEnd) {
        AsyncImage(
            model = notification.actorPhotoUrl,
            contentDescription = "User profile picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        val icon = when(notification.type) {
            NotificationType.FOLLOW_REQUEST -> Icons.Default.PersonAdd
            NotificationType.LIKE -> Icons.Default.Favorite
            NotificationType.COMMENT -> Icons.Default.Comment
            else -> null
        }
        val iconColor = when (notification.type) {
            NotificationType.FOLLOW_REQUEST -> Color.Blue.copy(alpha = 0.8f)
            NotificationType.LIKE -> Color.Red
            NotificationType.COMMENT -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        }

        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .background(iconColor, CircleShape)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun NotificationText(notification: Notification) {
    val annotatedText = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(notification.actorName)
        }
        val actionText = when (notification.type) {
            NotificationType.LIKE -> " liked your post."
            NotificationType.COMMENT -> " commented: '${notification.commentId}'"
            NotificationType.FOLLOW_REQUEST -> " started following you."
            NotificationType.COMMENT_LIKE -> " liked your comment: '${notification.commentId}'"
            else -> " sent you a notification."
        }
        append(actionText)
    }
    Text(text = annotatedText)
}

@Composable
fun NotificationAction(notification: Notification, onUserClick: () -> Unit) {
    when (notification.type) {
        NotificationType.FOLLOW_REQUEST -> {
            Button(
                onClick = { onUserClick() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0D7FF))
            ) {
                Text("Follow Back", color = MaterialTheme.colorScheme.primary)
            }
        }
        NotificationType.LIKE, NotificationType.COMMENT -> {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF0EBE0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = "Like icon", tint = Color(0xFFC8A063))
            }
        }
        else -> {
             if (notification.postPreviewUrl != null) {
                AsyncImage(
                    model = notification.postPreviewUrl,
                    contentDescription = "Post preview",
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
