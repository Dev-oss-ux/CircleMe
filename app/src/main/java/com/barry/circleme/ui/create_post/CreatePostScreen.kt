package com.barry.circleme.ui.create_post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barry.circleme.R

@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    createPostViewModel: CreatePostViewModel = viewModel(),
    onPostCreated: () -> Unit
) {
    val text by createPostViewModel.text.collectAsState()
    val postCreated by createPostViewModel.postCreated.collectAsState()

    LaunchedEffect(postCreated) {
        if (postCreated) {
            onPostCreated()
            createPostViewModel.onPostCreatedHandled() // Reset the state
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { createPostViewModel.onTextChange(it) },
                label = { Text(stringResource(R.string.whats_on_your_mind)) },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Button(
                onClick = { createPostViewModel.createPost() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.post))
            }
        }
    }
}
