package com.barry.circleme.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.Story
import com.barry.circleme.ui.create_post.Post
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    onSignOut: () -> Unit,
    onUserClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMessagesClick: () -> Unit
) {
    val posts by homeViewModel.posts.collectAsState()
    val stories by homeViewModel.stories.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    var viewedStory by remember { mutableStateOf<Story?>(null) }
    var showAddStoryDialog by remember { mutableStateOf(false) }
    var storyText by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { homeViewModel.createStory(it) }
    }

    if (viewedStory != null) {
        StoryViewer(story = viewedStory!!, onDismiss = { viewedStory = null })
    }

    if (showAddStoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddStoryDialog = false },
            title = { Text("Create a Story") },
            text = {
                Column {
                    Text("Share a moment with your followers.")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = storyText,
                        onValueChange = { storyText = it },
                        placeholder = { Text("Or just type something...") }
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { 
                        imagePickerLauncher.launch("image/*")
                        showAddStoryDialog = false
                     }) {
                        Text("Image")
                    }
                    TextButton(onClick = { 
                        if (storyText.isNotBlank()) {
                            homeViewModel.createStory(storyText = storyText)
                            showAddStoryDialog = false
                        }
                     }) {
                        Text("Text")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("CircleMe", fontFamily = FontFamily.Cursive) },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to notifications */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onMessagesClick) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Messages")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Stories
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                item {
                    AddStoryItem(onClick = { showAddStoryDialog = true })
                }
                items(stories) { story ->
                    StoryItem(story = story, onClick = { viewedStory = story }, modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (isLoading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(5) { 
                        PostCardSkeleton()
                    }
                }
            } else if (posts.isEmpty()) {
                Text("No posts found. Be the first to share!")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post, 
                            homeViewModel = homeViewModel,
                            onUserClick = onUserClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddStoryItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = "Add Story")
        }
        Text("Your Story", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun StoryItem(story: Story, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = story.userProfilePictureUrl, 
            contentDescription = "Story",
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )
        Text(story.username, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun StoryViewer(story: Story, onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        if (story.imageUrl != null && story.imageUrl.isNotBlank()) {
            AsyncImage(
                model = story.imageUrl,
                contentDescription = "Full screen story",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = story.text ?: "",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
