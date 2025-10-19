package com.barry.circleme.ui.chat

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barry.circleme.data.ChatMessage
import com.barry.circleme.data.MessageType
import com.barry.circleme.ui.theme.WhatsAppGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isSentByCurrentUser: Boolean
) {
    val bubbleColor = if (isSentByCurrentUser) WhatsAppGreen else Color.White
    val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .padding(8.dp)
        ) {
            when (message.type) {
                MessageType.TEXT -> TextMessageContent(message)
                MessageType.VOICE -> VoiceMessageContent(message)
            }
        }
    }
}

@Composable
fun TextMessageContent(message: ChatMessage) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = message.text ?: "",
            modifier = Modifier.padding(end = 8.dp).alignByBaseline()
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = message.timestamp?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.alignByBaseline()
        )
    }
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

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(200.dp)) {
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
            Text(
                text = "${formatDuration(currentPosition)} / ${formatDuration(totalDuration)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    return String.format("%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(millis),
        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    )
}
