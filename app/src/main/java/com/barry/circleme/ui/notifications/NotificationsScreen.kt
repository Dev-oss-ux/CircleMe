package com.barry.circleme.ui.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barry.circleme.data.Notification
import com.barry.circleme.data.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    notificationsViewModel: NotificationsViewModel = viewModel()
) {
    val notifications by notificationsViewModel.notifications.collectAsState()

    LaunchedEffect(Unit) {
        notificationsViewModel.markAllAsRead()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Notifications") }) }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(notification = notification)
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: Add user profile picture
        Spacer(modifier = Modifier.padding(start = 16.dp))
        Column {
            val text = when (notification.type) {
                NotificationType.LIKE -> "${notification.actorName} liked your post."
                NotificationType.COMMENT -> "${notification.actorName} commented on your post."
                else -> "New notification"
            }
            Text(text = text, fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
