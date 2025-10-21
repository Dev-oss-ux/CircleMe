package com.barry.circleme.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVideoCall: () -> Unit,
    onNavigateToVoiceCall: () -> Unit,
) {
    val chatViewModel: ChatViewModel = viewModel()
    val messages by chatViewModel.messages.collectAsState()
    val newMessageText by chatViewModel.newMessageText.collectAsState()
    val recipient by chatViewModel.recipient.collectAsState()
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(messages) {
        listState.animateScrollToItem(messages.size)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipient?.username ?: "") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = onNavigateToVideoCall) { Icon(Icons.Default.Videocam, "Video Call") }
                    IconButton(onClick = onNavigateToVoiceCall) { Icon(Icons.Default.Phone, "Voice Call") }
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("User Profile") }, onClick = { /* TODO */ })
                        DropdownMenuItem(text = { Text("Search") }, onClick = { /* TODO */ })
                        DropdownMenuItem(text = { Text("Clear History") }, onClick = { /* TODO */ })
                    }
                }
            )
        },
        bottomBar = {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = newMessageText,
                    onValueChange = { chatViewModel.onNewMessageChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { chatViewModel.sendTextMessage() }) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(messages) {
                val isSentByCurrentUser = it.senderId == Firebase.auth.currentUser?.uid
                ChatMessageItem(it, isSentByCurrentUser, chatViewModel, recipient)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}