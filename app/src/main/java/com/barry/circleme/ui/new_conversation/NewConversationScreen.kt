package com.barry.circleme.ui.new_conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConversationScreen(
    onUserClick: (String) -> Unit,
    newConversationViewModel: NewConversationViewModel = viewModel()
) {
    val users by newConversationViewModel.users.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Conversation") })
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(users) { user ->
                UserItem(user = user, onClick = { onUserClick(user.uid) })
            }
        }
    }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.photoUrl,
            contentDescription = "Profile picture of ${user.displayName}",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(user.displayName ?: "")
    }
}
