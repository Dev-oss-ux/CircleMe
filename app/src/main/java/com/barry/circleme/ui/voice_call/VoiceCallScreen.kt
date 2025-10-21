package com.barry.circleme.ui.voice_call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun VoiceCallScreen(onNavigateBack: () -> Unit, voiceCallViewModel: VoiceCallViewModel = viewModel()) {
    val recipient by voiceCallViewModel.recipient.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(recipient?.displayName ?: "", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text("01:23", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(120.dp))
        AsyncImage(
            model = recipient?.photoUrl,
            contentDescription = "Recipient profile picture",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(120.dp))
        Row(
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MicOff, contentDescription = "Mute", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.VolumeUp, contentDescription = "Speaker", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.CallEnd, contentDescription = "End call", tint = Color.Red, modifier = Modifier.size(48.dp))
            }
        }
    }
}
