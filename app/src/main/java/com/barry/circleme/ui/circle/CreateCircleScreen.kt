package com.barry.circleme.ui.circle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barry.circleme.R

@Composable
fun CreateCircleScreen(
    modifier: Modifier = Modifier,
    circleViewModel: CircleViewModel = viewModel(),
    onCircleCreated: () -> Unit
) {
    val circleName by circleViewModel.circleName.collectAsState()
    val circleCreated by circleViewModel.circleCreated.collectAsState()

    LaunchedEffect(circleCreated) {
        if (circleCreated) {
            onCircleCreated()
            circleViewModel.onCircleCreatedHandled()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.create_your_first_circle))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = circleName,
            onValueChange = { circleViewModel.onCircleNameChange(it) },
            label = { Text(stringResource(R.string.circle_name)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { circleViewModel.createCircle() }) {
            Text(stringResource(R.string.create_circle))
        }
    }
}
