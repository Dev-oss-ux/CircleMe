package com.barry.circleme.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.ui.create_post.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    onSignOut: () -> Unit,
    onUserClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val posts by homeViewModel.posts.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("CircleMe", fontFamily = FontFamily.Cursive) },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to notifications */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* TODO: Navigate to messages */ }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Messages")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Stories
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                items(10) { 
                    StoryItem(modifier = Modifier.padding(end = 8.dp))
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
                    contentPadding = PaddingValues(16.dp),
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
fun StoryItem(modifier: Modifier = Modifier) {
    AsyncImage(
        model = "https://picsum.photos/200", 
        contentDescription = "Story",
        modifier = modifier
            .size(70.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Gray, CircleShape)
    )
}
