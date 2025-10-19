package com.barry.circleme.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.Comment
import com.barry.circleme.ui.create_post.Post
import com.barry.circleme.utils.TimeUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun PostCard(
    post: Post,
    homeViewModel: HomeViewModel = viewModel()
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isLiked = currentUserId?.let { post.likedBy.contains(it) } ?: false
    var showPostMenu by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    val likerNames by homeViewModel.likerNames.collectAsState()
    var showLikers by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(post.id) {
        Firebase.firestore.collection("posts").document(post.id)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                comments = snapshots?.toObjects(Comment::class.java) ?: emptyList()
            }
    }

    if (showLikers) {
        LikesDialog(likerNames = likerNames, onDismiss = {
            showLikers = false
            homeViewModel.clearLikerNames()
        })
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- Post Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.authorPhotoUrl,
                contentDescription = "Author's profile picture",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Text(
                text = post.authorName,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            if (post.authorId == currentUserId) {
                Box {
                    IconButton(onClick = { showPostMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Post options")
                    }
                    DropdownMenu(
                        expanded = showPostMenu,
                        onDismissRequest = { showPostMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { /* TODO: Implement edit */ showPostMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                homeViewModel.deletePost(post.id)
                                showPostMenu = false
                            }
                        )
                    }
                }
            }
        }

        // --- Post Image ---
        if (!post.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Square aspect ratio
                contentScale = ContentScale.Crop
            )
        }

        // --- Action Buttons ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PostActionButton(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                onClick = { homeViewModel.toggleLike(post.id) }
            )
            PostActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                onClick = { showComments = !showComments }
            )
            PostActionButton(
                icon = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "Share",
                onClick = { /* TODO: Share post */ }
            )
            Spacer(modifier = Modifier.weight(1f))
            PostActionButton(
                icon = Icons.Outlined.BookmarkBorder,
                contentDescription = "Save",
                onClick = { /* TODO: Save post */ }
            )
        }

        // --- Likes and Caption ---
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            if (post.likedBy.isNotEmpty()) {
                Text(
                    text = "${post.likedBy.size} likes",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                     modifier = Modifier.clickable { 
                        if (post.likedBy.isNotEmpty()) {
                            homeViewModel.getLikerNames(post.likedBy)
                            showLikers = true
                        }
                    }
                )
            }
            if (post.text.isNotBlank()) {
                 Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(post.authorName)
                        }
                        append(" ")
                        append(post.text)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // --- Comments Section ---
            if (showComments) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    comments.forEach { comment ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(comment.authorName)
                                    }
                                    append(" ")
                                    append(comment.text)
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        TextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Add a comment...") }
                        )
                        IconButton(onClick = {
                            if (commentText.isNotBlank()) {
                                homeViewModel.addComment(post.id, commentText)
                                commentText = ""
                            }
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Send comment")
                        }
                    }
                }
            }

            Text(
                text = TimeUtils.formatTimestamp(post.timestamp),
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PostActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun LikesDialog(likerNames: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Liked by") },
        text = { 
            Column {
                likerNames.forEach { name ->
                    Text(name)
                }
            }
         },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
