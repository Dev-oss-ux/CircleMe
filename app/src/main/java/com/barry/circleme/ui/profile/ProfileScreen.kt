package com.barry.circleme.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.barry.circleme.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userId: String? = null,
    profileViewModel: ProfileViewModel = viewModel(),
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val displayName by profileViewModel.displayName.collectAsState()
    val photoUrl by profileViewModel.photoUrl.collectAsState()
    val postCount by profileViewModel.postCount.collectAsState()
    val followersCount by profileViewModel.followersCount.collectAsState()
    val followingCount by profileViewModel.followingCount.collectAsState()
    val userPostsWithImages by profileViewModel.userPostsWithImages.collectAsState()
    val bookmarkedPosts by profileViewModel.bookmarkedPosts.collectAsState()
    val signedOut by profileViewModel.signedOut.collectAsState()
    val profileUser by profileViewModel.profileUser.collectAsState()
    val followStatus by profileViewModel.followStatus.collectAsState()
    val isCurrentUserProfile by profileViewModel.isCurrentUserProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { profileViewModel.uploadProfileImage(it) }
    }

    LaunchedEffect(userId) {
        profileViewModel.loadProfile(userId)
    }

    LaunchedEffect(signedOut) {
        if (signedOut) {
            onSignOut()
            profileViewModel.onSignedOutHandled()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(profileUser?.username ?: "", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (!isCurrentUserProfile) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isCurrentUserProfile) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        if (isLoading) {
            ProfileScreenSkeleton()
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Bio Section ---
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(profileUser?.bio ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Action Buttons ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCurrentUserProfile) {
                            Button(
                                onClick = { onEditProfile() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Edit Profile")
                            }
                            Button(
                                onClick = { /* TODO: Share Profile */ },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                            ) {
                                Text("Share Profile", color = Color.Black)
                            }
                        } else {
                            Button(
                                onClick = { profileViewModel.handleFollowAction() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = when (followStatus) {
                                    FollowStatus.FOLLOWING -> "Unfollow"
                                    FollowStatus.NOT_FOLLOWING -> "Follow"
                                    FollowStatus.REQUESTED -> "Requested"
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Stats ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatColumn(title = "Posts", value = postCount.toString())
                        StatColumn(title = "Followers", value = followersCount.toString())
                        StatColumn(title = "Following", value = followingCount.toString())
                    }
                }

                // --- Post Grid ---
                val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(top = 16.dp)) {
                    Tab(selected = selectedTab == 0, onClick = { setSelectedTab(0) }) {
                         Icon(Icons.Default.GridView, "Grid", modifier = Modifier.padding(8.dp)) 
                    }
                    Tab(selected = selectedTab == 1, onClick = { setSelectedTab(1) }) {
                         Icon(if (selectedTab == 1) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, "Reels", modifier = Modifier.padding(8.dp)) 
                    }
                }
                
                val postsToShow = when (selectedTab) {
                    0 -> userPostsWithImages
                    1 -> bookmarkedPosts
                    else -> emptyList()
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(400.dp), // Adjust height as needed
                    userScrollEnabled = false // Disable scrolling for the grid itself
                ) {
                    items(postsToShow) { post ->
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = "User post image",
                            modifier = Modifier.aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = title, fontSize = 14.sp, color = Color.Gray)
    }
}
