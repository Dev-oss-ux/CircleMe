package com.barry.circleme.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import com.barry.circleme.ui.comments.CommentsScreen
import com.barry.circleme.ui.conversations.ConversationsViewModel
import com.barry.circleme.ui.discover.DiscoverScreen
import com.barry.circleme.ui.home.HomeScreen
import com.barry.circleme.ui.notifications.NotificationsScreen
import com.barry.circleme.ui.notifications.NotificationsViewModel
import com.barry.circleme.ui.post.PostScreen
import com.barry.circleme.ui.profile.EditProfileScreen
import com.barry.circleme.ui.profile.ProfileScreen
import com.barry.circleme.ui.settings.SettingsScreen

sealed class Screen(
    val route: String,
    val icon: @Composable (Boolean) -> Unit
) {
    object Home : Screen(Routes.HOME_SCREEN, { _ -> Icon(Icons.Filled.Home, contentDescription = null) })
    object Discover : Screen(Routes.DISCOVER_SCREEN, { _ -> Icon(Icons.Filled.Search, contentDescription = null) })
    object Notifications : Screen(Routes.NOTIFICATIONS_SCREEN, { hasNotification ->
        BadgedBox(badge = { if (hasNotification) Badge() }) {
            Icon(Icons.Filled.Notifications, contentDescription = null)
        }
    })
    object Profile : Screen(Routes.PROFILE_SCREEN, { _ -> Icon(Icons.Filled.Person, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appNavController: NavController) {
    val navController = rememberNavController()
    val notificationsViewModel: NotificationsViewModel = viewModel()
    val conversationsViewModel: ConversationsViewModel = viewModel()
    val hasUnreadNotifications by notificationsViewModel.hasUnreadNotifications.collectAsState(initial = false)
    val bottomBarItems = listOf(Screen.Home, Screen.Discover, Screen.Notifications, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomBarItems.take(2).forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon(hasUnreadNotifications) },
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

                NavigationBarItem(
                    selected = false,
                    onClick = { appNavController.navigate(Routes.CREATE_POST_SCREEN) },
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Create Post", modifier = Modifier.size(36.dp)) }
                )

                bottomBarItems.takeLast(2).forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon(hasUnreadNotifications) },
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
                        onMessagesClick = { appNavController.navigate(Routes.MESSAGES_SCREEN) },
                        onCommentsClick = { postId -> appNavController.navigate("${Routes.COMMENTS_SCREEN}/$postId") }
                    )
                }
                composable(Routes.DISCOVER_SCREEN) {
                    DiscoverScreen(
                        onUserClick = { userId -> navController.navigate("${Routes.PROFILE_SCREEN}/$userId") }
                    )
                }
                composable(Routes.NOTIFICATIONS_SCREEN) {
                    NotificationsScreen(
                        onNotificationClick = { postId -> navController.navigate("${Routes.POST_SCREEN}/$postId") },
                        onUserClick = { userId -> navController.navigate("${Routes.PROFILE_SCREEN}/$userId") },
                        onSettingsClick = { navController.navigate(Routes.SETTINGS_SCREEN) }
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
                        },
                        onBackClick = { navController.popBackStack() }
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
                        },
                        onBackClick = { navController.popBackStack() }
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
                            onUserClick = { userId -> navController.navigate("${Routes.PROFILE_SCREEN}/$userId") },
                            onCommentsClick = { appNavController.navigate("${Routes.COMMENTS_SCREEN}/$postId") }
                        )
                    }
                }
                composable(Routes.SETTINGS_SCREEN) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSignOut = {
                            appNavController.navigate(Routes.AUTH_SCREEN) {
                                popUpTo(Routes.MAIN_SCREEN) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
