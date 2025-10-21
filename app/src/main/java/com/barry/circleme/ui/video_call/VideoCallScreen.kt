package com.barry.circleme.ui.video_call

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VideocamOff
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

@Composable
fun VideoCallScreen(onNavigateBack: () -> Unit, videoCallViewModel: VideoCallViewModel = viewModel()) {
    val recipient by videoCallViewModel.recipient.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Remote video stream placeholder
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)) {
            Text("Remote Video", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        // Local video preview
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(width = 100.dp, height = 150.dp)
                .background(Color.Gray) // Placeholder for local video
        ) {
            Text("You", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(recipient?.displayName ?: "", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("12:34", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: Mute mic */ }) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.DarkGray.copy(alpha = 0.5f)).padding(12.dp)
                    )
                }
                IconButton(onClick = { /* TODO: Toggle video */ }) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = "Turn off video",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.DarkGray.copy(alpha = 0.5f)).padding(12.dp)
                    )
                }
                IconButton(onClick = { /* TODO: Switch camera */ }) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch camera",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.DarkGray.copy(alpha = 0.5f)).padding(12.dp)
                    )
                }
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End call",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.Red).padding(12.dp)
                    )
                }
            }
        }
    }
}
