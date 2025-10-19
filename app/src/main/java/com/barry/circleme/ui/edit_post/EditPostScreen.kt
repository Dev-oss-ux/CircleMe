package com.barry.circleme.ui.edit_post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    postId: String,
    initialText: String,
    onPostUpdated: () -> Unit,
    editPostViewModel: EditPostViewModel = viewModel()
) {
    val text by editPostViewModel.text.collectAsState()
    val postUpdated by editPostViewModel.postUpdated.collectAsState()

    // Set the initial text when the screen is first composed
    LaunchedEffect(Unit) {
        editPostViewModel.setInitialText(initialText)
    }

    // Navigate back when the post is successfully updated
    LaunchedEffect(postUpdated) {
        if (postUpdated) {
            editPostViewModel.onPostUpdatedHandled()
            onPostUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Post") })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { editPostViewModel.onTextChange(it) },
                label = { Text("Your text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(
                onClick = { editPostViewModel.updatePost(postId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
