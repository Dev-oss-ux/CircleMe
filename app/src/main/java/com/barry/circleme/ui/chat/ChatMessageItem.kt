package com.barry.circleme.ui.chat

import android.media.MediaPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.barry.circleme.data.ChatMessage
import com.barry.circleme.data.MessageType
import com.barry.circleme.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isSentByCurrentUser: Boolean,
    chatViewModel: ChatViewModel,
    recipient: User?
) {
    val bubbleColor = if (isSentByCurrentUser) MaterialTheme.colorScheme.primary else Color.LightGray
    val textColor = if (isSentByCurrentUser) Color.White else Color.Black
    val horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    var showReactions by remember { mutableStateOf(false) }

    val bubbleShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isSentByCurrentUser) {
            AsyncImage(
                model = recipient?.photoUrl,
                contentDescription = "Recipient profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            modifier = Modifier.combinedClickable(
                onClick = { /* TODO */ },
                onLongClick = { showReactions = true }
            )
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                when (message.type) {
                    MessageType.TEXT -> TextMessageContent(message, textColor)
                    MessageType.VOICE -> VoiceMessageContent(message)
                }
            }
        }
        if (isSentByCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = Firebase.auth.currentUser?.photoUrl,
                contentDescription = "My profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }

    if (showReactions) {
        EmojiReactionPicker(onEmojiSelected = {
            chatViewModel.addReaction(message.id, it)
            showReactions = false
        })
    }

    if (message.reactions.isNotEmpty()) {
        ReactionsSummary(reactions = message.reactions)
    }
}

@Composable
fun TextMessageContent(message: ChatMessage, color: Color) {
    Text(
        text = message.text ?: "",
        color = color
    )
}

@Composable
fun VoiceMessageContent(message: ChatMessage) {
    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    val totalDuration = remember { message.duration?.let { TimeUnit.SECONDS.toMillis(it) } ?: 0L }

    LaunchedEffect(message.audioUrl) {
        message.audioUrl?.let { 
            try {
                mediaPlayer.setDataSource(it)
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = mediaPlayer.currentPosition.toLong()
            delay(100L)
        }
    }

    mediaPlayer.setOnCompletionListener {
        isPlaying = false
        currentPosition = 0
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .widthIn(min = 180.dp, max = 250.dp)
    ) {
        IconButton(onClick = {
            if (isPlaying) mediaPlayer.pause() else mediaPlayer.start()
            isPlaying = !isPlaying
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause"
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { currentPosition = it.toLong(); mediaPlayer.seekTo(it.toInt()) },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(0f)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatDuration(currentPosition),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = message.timestamp?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmojiReactionPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("❤️", "😂", "😢", "😮", "👍", "🙏")
    Card(modifier = Modifier.padding(8.dp)) {
        Row(modifier = Modifier.padding(8.dp)) {
            emojis.forEach { emoji ->
                Text(
                    text = emoji,
                    modifier = Modifier.clickable { onEmojiSelected(emoji) }.padding(4.dp),
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun ReactionsSummary(reactions: List<com.barry.circleme.data.Reaction>) {
    Row(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
        Text(
            text = reactions.joinToString(" ") { it.emoji },
            fontSize = 12.sp
        )
    }
}

private fun formatDuration(millis: Long): String {
    return String.format("%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(millis),
        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    )
}
