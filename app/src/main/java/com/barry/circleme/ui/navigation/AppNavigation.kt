package com.barry.circleme.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barry.circleme.ui.auth.AuthScreen
import com.barry.circleme.ui.chat.ChatScreen
import com.barry.circleme.ui.comments.CommentsScreen
import com.barry.circleme.ui.conversations.ConversationsScreen
import com.barry.circleme.ui.create_post.CreatePostScreen
import com.barry.circleme.ui.video_call.VideoCallScreen
import com.barry.circleme.ui.voice_call.VoiceCallScreen
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
            CreatePostScreen(
                onPostCreated = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "${Routes.CHAT_SCREEN}/{recipientId}",
            arguments = listOf(navArgument("recipientId") { type = NavType.StringType })
        ) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToVideoCall = { recipientId -> navController.navigate("${Routes.VIDEO_CALL_SCREEN}/$recipientId") },
                onNavigateToVoiceCall = { recipientId -> navController.navigate("${Routes.VOICE_CALL_SCREEN}/$recipientId") }
            )
        }
        composable(Routes.MESSAGES_SCREEN) {
            ConversationsScreen(
                onConversationClick = { recipientId ->
                    navController.navigate("${Routes.CHAT_SCREEN}/$recipientId")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("${Routes.COMMENTS_SCREEN}/{postId}") {
            CommentsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "${Routes.VOICE_CALL_SCREEN}/{recipientId}",
            arguments = listOf(navArgument("recipientId") { type = NavType.StringType })
        ) {
            VoiceCallScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = "${Routes.VIDEO_CALL_SCREEN}/{recipientId}",
            arguments = listOf(navArgument("recipientId") { type = NavType.StringType })
        ) {
            VideoCallScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
