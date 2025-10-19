package com.barry.circleme.ui.chat

import android.Manifest
import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.R
import com.barry.circleme.data.ChatMessage
import com.barry.circleme.ui.theme.WhatsAppBackground
import com.barry.circleme.utils.VoiceRecorder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val messages by chatViewModel.messages.collectAsState()
    val recipient by chatViewModel.recipient.collectAsState()
    val listState = rememberLazyListState()
    val currentUserId = Firebase.auth.currentUser?.uid

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { 
            ChatTopAppBar(
                recipient = recipient,
                onNavigateBack = onNavigateBack
            )
         },
        bottomBar = { ChatInputBar(chatViewModel = chatViewModel) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(WhatsAppBackground).padding(paddingValues)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        isSentByCurrentUser = message.senderId == currentUserId
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(recipient: com.barry.circleme.data.User?, onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* TODO: Navigate to profile */ }
            ) {
                AsyncImage(
                    model = recipient?.photoUrl,
                    contentDescription = "Recipient profile picture",
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(recipient?.displayName ?: "")
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Videocam, contentDescription = "Video call")
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(chatViewModel: ChatViewModel) {
    val newMessageText by chatViewModel.newMessageText.collectAsState()
    val voiceRecordingState by chatViewModel.voiceRecordingState.collectAsState()
    val context = LocalContext.current
    val voiceRecorder = remember { VoiceRecorder(context) } 
    var recordingFile by remember { mutableStateOf<File?>(null) }

    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) { chatViewModel.onStartRecording() }
    }

    when (voiceRecordingState) {
        VoiceRecordingState.IDLE -> {
             Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .padding(bottom = 18.dp) // Added bottom padding
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.White).height(48.dp)) {
                        TextField(
                            value = newMessageText,
                            onValueChange = { chatViewModel.onNewMessageChange(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.type_a_message)) },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { /*TODO*/ }) {
                                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                                    }
                                    IconButton(onClick = { /*TODO*/ }) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            chatViewModel.sendTextMessage()
                        } else {
                            if (recordAudioPermissionState.status.isGranted) {
                                chatViewModel.onStartRecording()
                            } else {
                                launcher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (newMessageText.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                        contentDescription = if (newMessageText.isNotBlank()) "Send" else "Record",
                        tint = Color.White
                    )
                }
            }
        }

        VoiceRecordingState.RECORDING -> {
             LaunchedEffect(Unit) {
                val file = File(context.cacheDir, "voice_message.mp4")
                if(voiceRecorder.start(file)) {
                    recordingFile = file
                } else {
                    chatViewModel.onCancelRecording()
                }
            }
            RecordingUI(onStop = {
                voiceRecorder.stop()
                chatViewModel.onPreviewRecording()
            })
        }

        VoiceRecordingState.PREVIEW -> {
            PreviewUI(
                onSend = {
                    recordingFile?.let { file ->
                        if (file.exists() && file.length() > 0) {
                            val duration = getAudioDuration(file, context)
                            chatViewModel.sendVoiceMessage(file.toUri(), duration)
                        } else {
                            chatViewModel.onCancelRecording()
                        }
                        file.delete()
                    }
                },
                onCancel = {
                    recordingFile?.delete()
                    chatViewModel.onCancelRecording()
                },
                recordingFile = recordingFile
            )
        }
    }
}

@Composable
fun RecordingUI(onStop: () -> Unit) {
    var timer by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timer++
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = String.format("%02d:%02d", timer / 60, timer % 60))
        IconButton(onClick = onStop) {
            Icon(Icons.Default.Stop, contentDescription = "Stop recording")
        }
    }
}

@Composable
fun PreviewUI(onSend: () -> Unit, onCancel: () -> Unit, recordingFile: File?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Delete, contentDescription = "Delete recording")
        }
        // TODO: We can add a simple player here later if needed
        Text("Recording ready to be sent")
        IconButton(onClick = onSend) {
            Icon(Icons.Default.Send, contentDescription = "Send recording")
        }
    }
}

private fun getAudioDuration(file: File, context: Context): Long {
    if (!file.exists() || file.length() == 0L) return 0L
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, file.toUri())
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        durationStr?.toLongOrNull()?.div(1000) ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}
