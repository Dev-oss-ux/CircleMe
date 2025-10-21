package com.barry.circleme.ui.auth

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barry.circleme.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    onSignInSuccess: () -> Unit
) {
    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, "Welcome, ${state.user.displayName ?: state.user.email}!", Toast.LENGTH_SHORT).show()
                onSignInSuccess()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit // Idle or Loading
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "CircleMe",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 64.dp, bottom = 32.dp)
            )
            
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Email or Username", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = email,
                    onValueChange = { authViewModel.onEmailChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your email or username") },
                    singleLine = true,
                    shape = RoundedCornerShape(25.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                 Text("Password", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = password,
                    onValueChange = { authViewModel.onPasswordChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    shape = RoundedCornerShape(25.dp),
                )
            }
            
            TextButton(onClick = { /* TODO */ }, modifier = Modifier.align(Alignment.End)) {
                Text("Forgot Password?")
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { authViewModel.signInWithEmailPassword() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                ) {
                    Text("Login", fontSize = 18.sp)
                }
            }

            Row(modifier = Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f))
                Text("  OR  ", color = Color.Gray, fontSize = 12.sp)
                Divider(modifier = Modifier.weight(1f))
            }

            OutlinedButton(
                onClick = { 
                    coroutineScope.launch {
                        val credentialManager = CredentialManager.create(context)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        try {
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential
                            if (credential is GoogleIdTokenCredential) {
                                authViewModel.signInWithGoogleToken(credential.idToken)
                            } else {
                                Log.w("AuthScreen", "Received a non-Google credential")
                            }
                        } catch (e: Exception) {
                            Log.e("AuthScreen", "Google Sign-In failed", e)
                        }
                    }
                 },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Continue with Google", modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { /* TODO: Facebook Sign In */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Continue with Facebook", modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                Text("Don't have an account?", color = Color.Gray)
                TextButton(onClick = { /* TODO: Navigate to Sign Up */ }) {
                    Text("Sign Up")
                }
            }
        }
    }
}
