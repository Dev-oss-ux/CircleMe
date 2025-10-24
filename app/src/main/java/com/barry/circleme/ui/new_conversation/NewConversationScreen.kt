package com.barry.circleme.ui.new_conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.User
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConversationScreen(
    onStartConversation: (User, String?) -> Unit,
    newConversationViewModel: NewConversationViewModel = viewModel()
) {
    val users by newConversationViewModel.users.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Conversation") })
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(users) { user ->
                UserItem(user = user, onClick = {
                    selectedUser = user
                    messageText = ""
                    showDialog = true
                })
            }
        }

        if (showDialog && selectedUser != null) {
            LaunchedEffect(showDialog) {
                // give time for composition then request focus
                focusRequester.requestFocus()
            }

            // centered card dialog with backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // avatar with subtle border
                            Box(modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF6F8FA)), contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = selectedUser?.photoUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(selectedUser?.displayName ?: "", fontWeight = FontWeight.Bold)
                                Text(text = "@${selectedUser?.uid?.take(8) ?: "user"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { showDialog = false; selectedUser = null }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { if (it.length <= 500) messageText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .focusRequester(focusRequester),
                            placeholder = { Text("Write a friendly message...") },
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                showDialog = false
                                selectedUser?.let { onStartConversation(it, if (messageText.isBlank()) null else messageText) }
                                selectedUser = null
                            }),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFF0A84FF),
                                unfocusedIndicatorColor = Color(0xFFE6E9EE)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle.Default.copy(color = Color.Black)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "${messageText.length}/500", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))

                            Row {
                                TextButton(onClick = { showDialog = false; selectedUser = null }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    showDialog = false
                                    selectedUser?.let { onStartConversation(it, if (messageText.isBlank()) null else messageText) }
                                    selectedUser = null
                                }) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send")
                                }
                            }
                        }
                    }
                }
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
