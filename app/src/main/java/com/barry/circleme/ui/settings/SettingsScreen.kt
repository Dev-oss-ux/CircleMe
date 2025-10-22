package com.barry.circleme.ui.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.barry.circleme.ui.theme.ThemeViewModel
import com.barry.circleme.ui.theme.ThemeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit, onSignOut: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    val themeViewModelFactory = ThemeViewModelFactory(sharedPreferences)
    val themeViewModel = ViewModelProvider(LocalContext.current as androidx.lifecycle.ViewModelStoreOwner, themeViewModelFactory)[ThemeViewModel::class.java]
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    placeholder = { Text("Search settings") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            
            item { SectionTitle("Account") }
            item { SettingsItem(icon = Icons.Default.AccountCircle, title = "Profile Information", showArrow = true) }
            item { SettingsItem(icon = Icons.Default.Lock, title = "Change Password", showArrow = true) }
            item { SettingsItem(icon = Icons.Default.VpnKey, title = "Linked Accounts", showArrow = true) }

            item { SectionTitle("Privacy & Security") }
            item { SettingsItem(icon = Icons.Default.PrivacyTip, title = "Private Account", isSwitch = true) }
            item { SettingsItem(icon = Icons.Default.People, title = "Blocked Accounts", showArrow = true) }
            item { SettingsItem(icon = Icons.Default.Security, title = "Two-Factor Authentication", showArrow = true) }

            item { SectionTitle("Notifications") }
            item { SettingsItem(icon = Icons.Default.Notifications, title = "Push Notifications", showArrow = true) }
            item { SettingsItem(icon = Icons.Default.Email, title = "Email Notifications", isSwitch = true) }
            item { SettingsItem(icon = Icons.Default.PhonelinkLock, title = "In-App Notifications", isSwitch = true, isChecked = true) }

            item { SectionTitle("General") }
            item { SettingsItem(icon = Icons.Default.Language, title = "Language", value = "English", showArrow = true) }
            item { 
                SettingsItem(
                    icon = Icons.Default.DarkMode, 
                    title = "Dark Mode", 
                    isSwitch = true, 
                    isChecked = isDarkTheme,
                    onCheckedChange = { themeViewModel.toggleTheme() }
                ) 
            }
            item { SettingsItem(icon = Icons.Default.Storage, title = "Data Usage", showArrow = true) }
            
            item { SectionTitle("Support & Legal") }
            item { SettingsItem(title = "Help Center", showArrow = true) }
            item { SettingsItem(title = "Report a Problem", showArrow = true) }
            item { SettingsItem(title = "Terms of Service", showArrow = true) }
            item { SettingsItem(title = "Privacy Policy", showArrow = true) }

            item {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.Red)
                    Text("Logout", color = Color.Red, modifier = Modifier.padding(start = 8.dp))
                }
            }
            item {
                Text(
                    text = "CircleMe version 1.0.0",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector? = null,
    title: String,
    value: String? = null,
    isSwitch: Boolean = false,
    isChecked: Boolean = false,
    showArrow: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (showArrow) { /* TODO */ } }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = title, modifier = Modifier.padding(end = 16.dp))
        }
        Text(text = title, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(text = value, color = Color.Gray)
        }
        if (isSwitch) {
            Switch(checked = isChecked, onCheckedChange = onCheckedChange)
        }
        if (showArrow) {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray)
        }
    }
}
