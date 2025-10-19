package com.barry.circleme.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
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
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    val likerNames by homeViewModel.likerNames.collectAsState()
    val currentUserId = Firebase.auth.currentUser?.uid
    val isLiked = currentUserId?.let { post.likedBy.contains(it) } ?: false
    var showComments by remember { mutableStateOf(false) }
    var showLikers by remember { mutableStateOf(false) }
    var showPostMenu by remember { mutableStateOf(false) }

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.authorPhotoUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = post.authorPhotoUrl,
                        contentDescription = "Author's profile picture",
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(48.dp).background(Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Default profile picture", tint = Color.White)
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(text = post.authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = TimeUtils.formatTimestamp(post.timestamp), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
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
                                onClick = { 
                                    // TODO: Implement edit post
                                    showPostMenu = false 
                                }
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = post.text, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row {
                Text(
                    text = "${post.likedBy.size} likes", 
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { 
                        if (post.likedBy.isNotEmpty()) {
                            homeViewModel.getLikerNames(post.likedBy)
                            showLikers = true
                        }
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${comments.size} comments", style = MaterialTheme.typography.bodyMedium)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(icon = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp, text = "Like", onClick = { homeViewModel.toggleLike(post.id) })
                ActionButton(icon = Icons.Default.Comment, text = "Comment", onClick = { showComments = !showComments })
                ActionButton(icon = Icons.Default.Share, text = "Share", onClick = { /* TODO */ })
            }

            if (showComments) {
                // TODO: Add comments section here
            }
        }
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

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Text(text)
        }
    }
}
