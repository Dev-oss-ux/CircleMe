package com.barry.circleme.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barry.circleme.ui.conversations.ConversationsScreen
import com.barry.circleme.ui.conversations.ConversationsViewModel
import com.barry.circleme.ui.home.HomeScreen
import com.barry.circleme.ui.notifications.NotificationsScreen
import com.barry.circleme.ui.notifications.NotificationsViewModel
import com.barry.circleme.ui.post.PostScreen
import com.barry.circleme.ui.profile.EditProfileScreen
import com.barry.circleme.ui.profile.ProfileScreen
import com.barry.circleme.ui.settings.SettingsScreen

sealed class Screen(
    val route: String,
    val icon: @Composable (Int, Boolean) -> Unit
) {
    object Home : Screen(Routes.HOME_SCREEN, { _, _ -> Icon(Icons.Filled.Home, contentDescription = null) })
    object Messages : Screen(Routes.MESSAGES_SCREEN, { unreadCount, _ ->
        BadgedBox(badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }) {
            Icon(Icons.Filled.Message, contentDescription = null)
        }
    })
    object Notifications : Screen(Routes.NOTIFICATIONS_SCREEN, { _, hasNotification ->
        BadgedBox(badge = { if (hasNotification) Badge() }) {
            Icon(Icons.Filled.Notifications, contentDescription = null)
        }
    })
    object Profile : Screen(Routes.PROFILE_SCREEN, { _, _ -> Icon(Icons.Filled.Person, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appNavController: NavController) {
    val navController = rememberNavController()
    val notificationsViewModel: NotificationsViewModel = viewModel()
    val conversationsViewModel: ConversationsViewModel = viewModel()
    val hasUnreadNotifications by notificationsViewModel.hasUnreadNotifications.collectAsState(initial = false)
    val totalUnreadMessages by conversationsViewModel.totalUnreadCount.collectAsState(initial = 0)
    val bottomBarItems = listOf(Screen.Home, Screen.Messages, Screen.Notifications, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Items 1 & 2
                bottomBarItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon(totalUnreadMessages, hasUnreadNotifications) },
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
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = Routes.HOME_SCREEN) {
                composable(Routes.HOME_SCREEN) {
                    HomeScreen(
                        onSignOut = {},
                        onUserClick = { userId -> navController.navigate("${Routes.PROFILE_SCREEN}/$userId") },
                        onSearchClick = { appNavController.navigate(Routes.FIND_USER_SCREEN) }
                    )
                }
                composable(Routes.MESSAGES_SCREEN) {
                    ConversationsScreen(
                        onConversationClick = { recipientId ->
                            appNavController.navigate("${Routes.CHAT_SCREEN}/$recipientId")
                        }
                    )
                 }
                composable(Routes.NOTIFICATIONS_SCREEN) {
                    NotificationsScreen(
                        onNotificationClick = { postId -> navController.navigate("${Routes.POST_SCREEN}/$postId") },
                        modifier = TODO(),
                        notificationsViewModel = TODO(),
                        onUserClick = TODO()
                    )
                }
                composable(Routes.PROFILE_SCREEN) {
                    ProfileScreen(
                        onEditProfile = { navController.navigate(Routes.EDIT_PROFILE_SCREEN) },
                        onSettingsClick = { navController.navigate(Routes.SETTINGS_SCREEN) },
                        onSignOut = {
                            appNavController.navigate(Routes.AUTH_SCREEN) {
                                popUpTo(Routes.MAIN_SCREEN) { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = "${Routes.PROFILE_SCREEN}/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId")
                    ProfileScreen(
                        userId = userId,
                        onEditProfile = {},
                        onSettingsClick = {},
                        onSignOut = {
                            appNavController.navigate(Routes.AUTH_SCREEN) {
                                popUpTo(Routes.MAIN_SCREEN) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.EDIT_PROFILE_SCREEN) {
                    EditProfileScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${Routes.POST_SCREEN}/{postId}",
                    arguments = listOf(navArgument("postId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId")
                    if (postId != null) {
                        PostScreen(
                            postId = postId,
                            onNavigateBack = { navController.popBackStack() },
                            onUserClick = { userId -> navController.navigate("${Routes.PROFILE_SCREEN}/$userId") }
                        )
                    }
                }
                composable(Routes.SETTINGS_SCREEN) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
