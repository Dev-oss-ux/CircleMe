package com.barry.circleme.ui.comments

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.Comment
import com.barry.circleme.utils.TimeUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    commentsViewModel: CommentsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val post by commentsViewModel.post.collectAsState()
    val newCommentText by commentsViewModel.newCommentText.collectAsState()
    val currentUser = Firebase.auth.currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            CommentInputBar(
                userPhotoUrl = currentUser?.photoUrl?.toString(),
                value = newCommentText,
                onValueChange = { commentsViewModel.onNewCommentChange(it) },
                onSendClick = { commentsViewModel.addComment() }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            post?.let {
                item {
                    // Original Post Caption
                    CommentItem(
                        authorName = it.authorName,
                        authorPhotoUrl = it.authorPhotoUrl,
                        text = it.text,
                        timestamp = TimeUtils.formatTimestamp(it.timestamp)
                    )
                }
                items(it.comments) { comment ->
                    CommentItem(
                        authorName = comment.authorName,
                        authorPhotoUrl = comment.authorPhotoUrl,
                        text = comment.text,
                        timestamp = TimeUtils.formatTimestamp(comment.timestamp),
                        likes = comment.likedBy.size,
                        onLikeClick = { commentsViewModel.likeComment(comment.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    authorName: String,
    authorPhotoUrl: String?,
    text: String,
    timestamp: String,
    likes: Int = 0,
    onLikeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = authorPhotoUrl,
            contentDescription = "Profile picture of $authorName",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row {
                Text(text = authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = timestamp, color = Color.Gray, fontSize = 12.sp)
            }
            Text(text = text, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Like")
                }
                if (likes > 0) {
                    Text(text = "$likes", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = { /* TODO: Reply */ }) {
                    Text("Reply", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CommentInputBar(
    userPhotoUrl: String?,
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userPhotoUrl,
            contentDescription = "Your profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Add a comment...") },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        IconButton(onClick = onSendClick) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send comment")
        }
    }
}
