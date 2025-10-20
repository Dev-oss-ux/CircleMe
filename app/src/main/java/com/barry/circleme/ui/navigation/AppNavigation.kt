package com.barry.circleme.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.barry.circleme.ui.auth.AuthScreen
import com.barry.circleme.ui.chat.ChatScreen
import com.barry.circleme.ui.conversations.ConversationsViewModel
import com.barry.circleme.ui.create_post.CreatePostScreen
import com.barry.circleme.ui.find_user.FindUserScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val startDestination = if (Firebase.auth.currentUser != null) Routes.MAIN_SCREEN else Routes.AUTH_SCREEN

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.AUTH_SCREEN) {
            AuthScreen(onSignInSuccess = {
                navController.navigate(Routes.MAIN_SCREEN) {
                    popUpTo(Routes.AUTH_SCREEN) { inclusive = true }
                }
            })
        }
        composable(Routes.MAIN_SCREEN) {
            MainScreen(appNavController = navController)
        }
        composable(
            Routes.CREATE_POST_SCREEN,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut() }
        ) {
            CreatePostScreen(onPostCreated = { navController.popBackStack() })
        }
        composable("${Routes.CHAT_SCREEN}/{recipientId}") {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FIND_USER_SCREEN) {
            val conversationsViewModel: ConversationsViewModel = viewModel()
            FindUserScreen(
                onUserClick = { user ->
                    conversationsViewModel.startConversation(user) {
                        navController.navigate("${Routes.CHAT_SCREEN}/${user.uid}")
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
