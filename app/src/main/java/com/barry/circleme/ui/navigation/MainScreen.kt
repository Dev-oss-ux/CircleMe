package com.barry.circleme.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barry.circleme.R
import com.barry.circleme.ui.conversations.ConversationsScreen
import com.barry.circleme.ui.conversations.ConversationsViewModel
import com.barry.circleme.ui.home.HomeScreen
import com.barry.circleme.ui.notifications.NotificationsScreen
import com.barry.circleme.ui.notifications.NotificationsViewModel
import com.barry.circleme.ui.profile.ProfileScreen

sealed class Screen(val route: String, val resourceId: Int, val icon: @Composable (Boolean, Boolean) -> Unit) {
    object Home : Screen("home", R.string.home, { _, _ -> Icon(Icons.Filled.Home, contentDescription = null) })
    object Messages : Screen("messages", R.string.messages, { hasUnread, _ ->
        BadgedBox(badge = { if (hasUnread) Badge() }) {
            Icon(Icons.Filled.Message, contentDescription = null)
        }
    })
    object Notifications : Screen("notifications", R.string.notifications, { _, hasNotification ->
        BadgedBox(badge = { if (hasNotification) Badge() }) {
            Icon(Icons.Filled.Notifications, contentDescription = null)
        }
    })
    object Profile : Screen("profile", R.string.profile, { _, _ -> Icon(Icons.Filled.Person, contentDescription = null) })
}

val items = listOf(
    Screen.Home,
    Screen.Messages,
    Screen.Notifications,
    Screen.Profile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appNavController: androidx.navigation.NavController) {
    val navController = rememberNavController()
    val notificationsViewModel: NotificationsViewModel = viewModel()
    val conversationsViewModel: ConversationsViewModel = viewModel()
    val hasUnreadNotifications by notificationsViewModel.hasUnreadNotifications.collectAsState()
    val hasUnreadMessages by conversationsViewModel.hasUnreadMessages.collectAsState()

    Scaffold(
        bottomBar = {
            BottomAppBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val bottomNavItems = items.take(2) + items.takeLast(2)

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon(hasUnreadMessages, hasUnreadNotifications) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { appNavController.navigate(Routes.CREATE_POST_SCREEN) },
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
                composable(Screen.Home.route) { 
                    HomeScreen(onSignOut = {}, onCreatePost = { appNavController.navigate(Routes.CREATE_POST_SCREEN) })
                }
                composable(Screen.Messages.route) { 
                    ConversationsScreen(
                        onConversationClick = { recipientId ->
                            appNavController.navigate("${Routes.CHAT_SCREEN}/$recipientId")
                        }
                    )
                 }
                composable(Screen.Notifications.route) {
                    NotificationsScreen()
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
}
