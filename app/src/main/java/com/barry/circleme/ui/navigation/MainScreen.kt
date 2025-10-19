package com.barry.circleme.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barry.circleme.R
import com.barry.circleme.ui.conversations.ConversationsScreen
import com.barry.circleme.ui.home.HomeScreen
import com.barry.circleme.ui.profile.ProfileScreen

sealed class Screen(val route: String, val resourceId: Int, val icon: @Composable () -> Unit) {
    object Home : Screen("home", R.string.home, { Icon(Icons.Default.Home, contentDescription = null) })
    object Messages : Screen("messages", R.string.messages, { Icon(Icons.Default.Message, contentDescription = null) })
    object Profile : Screen("profile", R.string.profile, { Icon(Icons.Default.Person, contentDescription = null) })
}

val items = listOf(
    Screen.Home,
    Screen.Messages,
    Screen.Profile
)

@Composable
fun MainScreen(appNavController: androidx.navigation.NavController) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon() },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    onCreatePost = { appNavController.navigate(Routes.CREATE_POST_SCREEN) }, 
                    onSignOut = {}
                ) 
            }
            composable(Screen.Messages.route) { 
                ConversationsScreen(
                    onConversationClick = { recipientId ->
                        appNavController.navigate("${Routes.CHAT_SCREEN}/$recipientId")
                    }
                )
             }
            composable(Screen.Profile.route) {
                ProfileScreen(onSignOut = {
                    appNavController.navigate(Routes.AUTH_SCREEN) {
                        popUpTo(Routes.MAIN_SCREEN) { inclusive = true }
                    }
                })
            }
        }
    }
}
