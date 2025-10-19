package com.barry.circleme.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.barry.circleme.data.ChatMessage
import com.barry.circleme.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isSentByCurrentUser: Boolean
) {
    val bubbleColor = if (isSentByCurrentUser) WhatsAppGreen else Color.White
    val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val cornerRadius = 12.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(bubbleColor)
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = message.text,
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
    }
}
