package com.barry.circleme.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.Comment
import com.barry.circleme.data.User
import com.barry.circleme.ui.create_post.Post
import com.barry.circleme.utils.TimeUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun PostCard(
    post: Post,
    homeViewModel: HomeViewModel = viewModel(),
    onUserClick: (String) -> Unit,
    onCommentsClick: (String) -> Unit
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    var currentUser by remember { mutableStateOf<User?>(null) }
    val isLiked = currentUserId?.let { post.likedBy.contains(it) } ?: false
    val isBookmarked = currentUser?.bookmarkedPosts?.contains(post.id) ?: false
    var showPostMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var updatedText by remember { mutableStateOf(post.text) }
    val comments = post.comments
    val likerNames by homeViewModel.likerNames.collectAsState()
    var showLikers by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            Firebase.firestore.collection("users").document(currentUserId)
                .addSnapshotListener { snapshot, _ ->
                    currentUser = snapshot?.toObject(User::class.java)
                }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Post") },
            text = {
                TextField(
                    value = updatedText,
                    onValueChange = { updatedText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    homeViewModel.updatePost(post.id, updatedText)
                    showEditDialog = false
                 }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
                Button(onClick = { 
                    homeViewModel.deletePost(post.id)
                    showDeleteDialog = false
                 }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .clickable { onUserClick(post.authorId) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.authorPhotoUrl,
                contentDescription = "User profile picture",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = post.authorName,
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(TimeUtils.formatTimestamp(post.timestamp), style = MaterialTheme.typography.bodySmall)
            Box {
                IconButton(onClick = { showPostMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = showPostMenu,
                    onDismissRequest = { showPostMenu = false }
                ) {
                    if (post.authorId == currentUserId) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showEditDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showDeleteDialog = true }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Report") },
                            onClick = { /* TODO: Report Post */ }
                        )
                    }
                }
            }
        }

        // --- Image ---
        if (post.imageUrl?.isNotBlank() == true) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }

        // --- Action Buttons ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            PostActionButton(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                onClick = { homeViewModel.onLikeClick(post.id, post.authorId) },
                tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
            )
            PostActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                onClick = { onCommentsClick(post.id) }
            )
            PostActionButton(
                icon = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "Share",
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out this post by ${post.authorName}: ${post.text}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            PostActionButton(
                icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = "Save",
                onClick = { homeViewModel.toggleBookmark(post.id) }
            )
        }

        // --- Likes and Caption ---
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            if (post.likedBy.isNotEmpty()) {
                Text(
                    text = "${post.likedBy.size} likes",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (post.text.isNotBlank()) {
                 Text(
                    buildAnnotatedString {                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(post.authorName)
                        }
                        append(" ")
                        append(post.text)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // --- Comments Section ---
            if (comments.isNotEmpty()) {
                Text(
                    text = "View all ${comments.size} comments",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onCommentsClick(post.id) }
                )
            }
        }
    }
    
    if (showLikers) {
        LaunchedEffect(post.id) {
            homeViewModel.fetchLikers(post.id)
        }
        LikesDialog(likerNames = likerNames, onDismiss = { showLikers = false })
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    homeViewModel: HomeViewModel,
    postId: String,
    modifier: Modifier = Modifier
) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isLiked = currentUserId?.let { comment.likedBy.contains(it) } ?: false
    var showReplyTextField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(start = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { homeViewModel.likeComment(postId, comment.id) }) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like comment",
                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (comment.likedBy.isNotEmpty()) {
                Text("${comment.likedBy.size} likes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.padding(start = 8.dp))
            Text("Reply", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.clickable { showReplyTextField = !showReplyTextField })
        }

        if (showReplyTextField) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a reply...") }
                )
                IconButton(onClick = { 
                    homeViewModel.replyToComment(postId, comment.id, replyText)
                    replyText = ""
                    showReplyTextField = false
                }) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send reply")
                }
            }
        }

        comment.replies.forEach { reply ->
            CommentItem(comment = reply, homeViewModel = homeViewModel, postId = postId, modifier = Modifier.padding(start = 16.dp))
        }
    }
}


@Composable
fun PostActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun LikesDialog(likerNames: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Liked by") },
        text = {
            LazyColumn {
                items(likerNames) { name ->
                    Text(name, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}