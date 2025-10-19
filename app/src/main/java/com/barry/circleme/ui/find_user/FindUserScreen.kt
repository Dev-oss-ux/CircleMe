package com.barry.circleme.ui.find_user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barry.circleme.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindUserScreen(
    modifier: Modifier = Modifier,
    findUserViewModel: FindUserViewModel = viewModel(),
    onUserClick: (User) -> Unit
) {
    val users by findUserViewModel.users.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Find a User") }) }
    ) {
        LazyColumn(contentPadding = it) {
            items(users) {
                user ->
                ListItem(
                    headlineContent = { Text(user.displayName ?: "") },
                    // TODO: Add user avatar
                    modifier = Modifier.clickable { onUserClick(user) }
                )
            }
        }
    }
}
